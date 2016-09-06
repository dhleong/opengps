package net.dhleong.rxconnectr;

import com.jakewharton.rxrelay.BehaviorRelay;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import flightsim.simconnect.NotificationPriority;
import flightsim.simconnect.SimConnect;
import flightsim.simconnect.SimConnectConstants;
import flightsim.simconnect.SimConnectPeriod;
import flightsim.simconnect.recv.DispatcherTask;
import flightsim.simconnect.recv.ExceptionHandler;
import flightsim.simconnect.recv.OpenHandler;
import flightsim.simconnect.recv.RecvException;
import flightsim.simconnect.recv.RecvOpen;
import flightsim.simconnect.recv.RecvSimObjectData;
import flightsim.simconnect.recv.SimObjectDataHandler;
import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Main entry point to RxConnectr, a reactive wrapper library
 *  on top of jSimConnect.
 *
 * You should use ONE RxConnectr for ONE connection configuration.
 *  If you change the host/port, you should {@link #close()} the
 *  old one and create a new one.
 */
public class RxConnectr {

    public enum State {
        /** Not connected and not trying to */
        DISCONNECTED,

        /** Trying to connect */
        CONNECTING,

        /** Connected! */
        CONNECTED
    }

    private static final int DEFAULT_INTERVAL = 1;
    private static final TimeUnit DEFAULT_INTERVAL_UNIT = TimeUnit.SECONDS;

    private static final int RECONNECT_TIMEOUT = 5;
    private static final TimeUnit RECONNECT_TIMEOUT_UNIT = TimeUnit.SECONDS;

    static final int CLIENT_ID = 0;

    final String appName;
    final String host;
    final int port;

    final SimConnectHandler handler = new SimConnectHandler();
    final AtomicInteger nextDataTypeId = new AtomicInteger();
    final AtomicInteger nextEventId = new AtomicInteger();

    final BehaviorRelay<State> stateChanges = BehaviorRelay.create(State.DISCONNECTED);
    final HashMap<Class<?>, ObjectFactory<?>> factories = new HashMap<>();
    final HashMap<String, Integer> eventIds = new HashMap<>();
    final BehaviorRelay<SimConnect> connection = BehaviorRelay.create();
    PublishSubject<RecvSimObjectData> dataObjects = PublishSubject.create();
    PublishSubject<PendingEvent> eventQueue = PublishSubject.create();

    DispatchThread thread;
    boolean isOpen;
    SimConnect currentConn;
    Subscription eventQueueSub;


    public RxConnectr(String appName, String host, int port) {
        this.appName = appName;
        this.host = host;
        this.port = port;
    }

    /**
     * Register a data type that will be dynamically built
     *  for you using reflection. The class should have
     *  fields annotated with {@link ConnectrField}
     *
     * @see ConnectrField
     */
    public <T> void registerObjectType(Class<T> type) {
        registerObjectType(type, new ReflectiveObjectFactory<>(type));
    }

    public <T> void registerObjectType(Class<T> type, ObjectFactory<T> factory) {
        factories.put(type, factory);
    }

    public void open() {
        if (isOpen) throw new IllegalStateException("Already open");
        isOpen = true;

        openInternal();
    }

    private void openInternal() {
        if (thread != null) {
            throw new IllegalStateException("Already open");
        }

        nextDataTypeId.set(0);
        nextEventId.set(0);

        // update state
        stateChanges.call(State.CONNECTING);

        Observable.fromCallable(() -> new SimConnect(appName, host, port))
        .subscribeOn(Schedulers.io())
        .flatMap(sc -> Observable.from(factories.values()).flatMap(factory -> Observable.fromCallable(() -> {
            final int id = nextDataTypeId.getAndIncrement();
            factory.bindToDataDefinition(sc, id);
            return sc;
        })).last())
        .subscribe(conn -> {
            currentConn = conn;

            if (!isOpen) {
                closeInternal();
                return;
            }

            DispatcherTask task = new DispatcherTask(conn);
            task.addOpenHandler(handler);
            task.addExceptionHandler(handler);
            task.addSimObjectDataHandler(handler);

            thread = new DispatchThread(conn, task);
            thread.start();

            eventQueueSub = eventQueue.observeOn(Schedulers.io())
                .serialize()
                .map(pending -> {
                    Integer existingId = eventIds.get(pending.eventName);
                    if (existingId != null) {
                        pending.eventId = existingId;
                    } else {
                        int id = nextEventId.getAndIncrement();
                        pending.eventId = id;
                        eventIds.put(pending.eventName, id);

                        try {
                            conn.mapClientEventToSimEvent(id, pending.eventName);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    return pending;
                })
                .observeOn(Schedulers.io())
                .subscribe(pendingEvent -> {
                    try {
                        conn.transmitClientEvent(
                            CLIENT_ID, pendingEvent.eventId, pendingEvent.param,
                            NotificationPriority.DEFAULT.ordinal(),
                            SimConnectConstants.EVENT_FLAG_GROUPID_IS_PRIORITY
                            );
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        }, e -> {
            if (isOpen) {
                System.err.println("Error..." + e.getClass() + ": " + e.getMessage());
            }

            reconnect();
        });
    }

    public void sendEvent(String eventName, int param) {
        eventQueue.onNext(new PendingEvent(eventName, param));
    }

    public Observable<State> state() {
        return stateChanges;
    }

    public <T> Observable<T> subscribe(Class<T> type) {
        return subscribe(type, DEFAULT_INTERVAL, DEFAULT_INTERVAL_UNIT);
    }

    public <T> Observable<T> subscribe(Class<T> type, int minInterval, TimeUnit minIntervalUnit) {
        //noinspection unchecked always safe
        final ObjectFactory<T> factory = (ObjectFactory<T>) factories.get(type);
        if (factory == null) {
            throw new IllegalArgumentException(type + " is not registered");
        }

        return connection.flatMap(sc -> {
            int id = factory.getDataId();
            try {

                SimConnectPeriod period = SimConnectPeriod.SIM_FRAME;
                if (minIntervalUnit.toMillis(minInterval) >= 1000) {
                    // longer than a second, so we don't need SIM_FRAME period;
                    period = SimConnectPeriod.SECOND;
                }

                sc.requestDataOnSimObject(id, id, CLIENT_ID, period);
                return dataObjects.filter(obj -> obj.getRequestID() == id)
                                  .throttleLast(minInterval, minIntervalUnit)
                                  .map(factory::create)
                                  .doOnUnsubscribe(() -> {
                                      // unsubscribe from the request as well
                                      try {
                                          sc.requestDataOnSimObject(id, id, CLIENT_ID,
                                              SimConnectPeriod.NEVER);
                                      } catch (IOException e) {
                                          e.printStackTrace();
                                      }
                                  });
            } catch (IOException e) {
                return Observable.error(e);
            }
        });
    }

    public void close() {
        if (!isOpen) throw new IllegalStateException("Not open");
        isOpen = false;

        closeInternal();
    }

    void closeInternal() {

        stateChanges.call(State.DISCONNECTED);

        Subscription eventQueueSub = this.eventQueueSub;
        if (eventQueueSub != null) {
            eventQueueSub.unsubscribe();
        }
        eventIds.clear();

        final SimConnect conn = currentConn;
        currentConn = null;
        if (conn != null) {
            Observable.just(conn)
                      .subscribeOn(Schedulers.io())
                      .subscribe(c -> {
                          try {
                              c.close();
                          } catch (IOException e) {
                              // don't care
                          }
                      });
        }

        // reset the dataObjects stream
        dataObjects.onCompleted();
        dataObjects = PublishSubject.create();

        DispatchThread thread = this.thread;
        if (thread == null) {
            // didn't finish opening yet
            return;
        }
        this.thread = null;

        thread.cancel();
    }

    void reconnect() {
        DispatchThread thread = this.thread;
        if (thread != null) {
            thread.willReconnect = true;
        }

        // hahaha lazy
        try {
            closeInternal();
        } catch (IllegalStateException e) {
            // don't care
        }

        // NB: don't reconnect if we requested to close
        if (isOpen) {
            stateChanges.call(State.CONNECTING);

            // re-open after a delay
            Observable.just(null)
                      .delay(RECONNECT_TIMEOUT, RECONNECT_TIMEOUT_UNIT)
                      .subscribe(any -> openInternal());
        }
    }

    class SimConnectHandler
            implements SimObjectDataHandler,
                       OpenHandler,
                       ExceptionHandler {

        @Override
        public void handleSimObject(SimConnect simConnect, RecvSimObjectData recvSimObjectData) {
            dataObjects.onNext(recvSimObjectData);
        }

        @Override
        public void handleOpen(SimConnect simConnect, RecvOpen recvOpen) {
            stateChanges.call(State.CONNECTED);
            connection.call(simConnect);
        }

        @Override
        public void handleException(SimConnect simConnect, RecvException e) {
            System.err.println(e + e.getException().getMessage());
        }
    }

    class DispatchThread extends Thread {
        private final SimConnect sc;
        private final DispatcherTask task;

        volatile boolean running = true;
        volatile boolean willReconnect;

        public DispatchThread(SimConnect sc, DispatcherTask task) {
            this.sc = sc;
            this.task = task;
        }

        @Override
        public void run() {

            IOException notifyIoe = null;
            while (running) {
                try {
                    sc.callDispatch(task);
                } catch (IOException e) {
                    if (running) {
                        notifyIoe = e;
                    }
                    break;
                }
            }

            try {
                sc.close();
            } catch (IOException e) {
                // probably don't care
                e.printStackTrace();
            }

            if (notifyIoe != null) {
                // TODO do something with it?
                notifyIoe.printStackTrace();
            }

            if (isOpen && !willReconnect) {
                reconnect();
            }
        }

        public void cancel() {
            running = false;
        }
    }

    static class PendingEvent {
        final String eventName;
        final int param;
        int eventId;

        PendingEvent(String eventId, int param) {
            this.eventName = eventId;
            this.param = param;
        }
    }
}
