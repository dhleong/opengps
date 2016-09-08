package net.dhleong.opengps.modules;

import com.jakewharton.rxrelay.PublishRelay;

import net.dhleong.opengps.connection.ConnectionConfiguration;
import net.dhleong.opengps.connection.ConnectionDelegate;
import net.dhleong.opengps.connection.SimConnectConnection;
import net.dhleong.opengps.connection.data.RadioData;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.observables.ConnectableObservable;
import timber.log.Timber;

/**
 * @author dhleong
 */
public class RxChangingConnectionDelegate implements ConnectionDelegate {
    private final Observable<ConnectionDelegate> connection;

    ConnectionDelegate currentDelegate;
    Subscription subs;

    LocalDataMerger<RadioData> radioUpdater;

    @Inject RxChangingConnectionDelegate(Observable<ConnectionConfiguration> configs) {
         connection = configs.map(config -> {
             final ConnectionDelegate old = currentDelegate;
             currentDelegate = null;
             if (old != null) {
                 Timber.v("Close %s", old);
                 try {
                     old.close();
                 } catch (Throwable e) {
                     e.printStackTrace();
                 }
             }

             return initConnectionFromConfig(config);
        }).doOnNext(conn -> {
             // save and open
             Timber.v("OPEN %s", conn);
             currentDelegate = conn;
             conn.open();
        }).share()
          .replay(1);
    }

    @Override
    public void close() {
        final ConnectionDelegate delegate = currentDelegate;
        currentDelegate = null;
        if (delegate != null) {
            delegate.close();
        }
        Timber.v("CLOSE %s", delegate);

        final Subscription subs = this.subs;
        this.subs = null;
        if (subs != null) subs.unsubscribe();
    }

    @Override
    public void open() {
        if (subs != null) throw new IllegalStateException("Already open");
//        subs = connection.subscribe();
        subs = ((ConnectableObservable) connection).connect();
    }

    @Override
    public Observable<State> state() {
        return connection.flatMap(conn ->
            conn.state()
                .takeWhile(any -> currentDelegate == conn));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Observable<T> subscribe(Class<T> type) {
        Observable<T> base = connection.flatMap(conn -> conn.subscribe(type));

        // FIXME NB: if we subscribe to the same data in multiple places, this breaks
        // We don't do that right now, so lazy is fine
        if (type == RadioData.class) {
            radioUpdater = LocalDataMerger.wrap((Observable<RadioData>) base);
            return (Observable<T>) radioUpdater.merged();
        }

        return base;
    }

    @Override
    public void swapCom1() {
        currentDelegate.swapCom1();
        if (radioUpdater != null) {
            radioUpdater.update(radioData -> {
                float oldActive = radioData.com1active;
                radioData.com1active = radioData.com1standby;
                radioData.com1standby = oldActive;
            });
        }
    }

    @Override
    public void swapNav1() {
        currentDelegate.swapNav1();
        if (radioUpdater != null) {
            radioUpdater.update(radioData -> {
                float oldActive = radioData.nav1active;
                radioData.nav1active = radioData.nav1standby;
                radioData.nav1standby = oldActive;
            });
        }
    }

    @Override
    public void setTransmitCom2(boolean transmitCom2) {
        currentDelegate.setTransmitCom2(transmitCom2);
        if (radioUpdater != null) {
            radioUpdater.update(radioData -> {
                radioData.comTransmit1 = !transmitCom2;
                radioData.comTransmit2 = transmitCom2;
            });
        }
    }

    @Override
    public void setReceiveAll(boolean receiveAll) {
        currentDelegate.setReceiveAll(receiveAll);
        if (radioUpdater != null) {
            radioUpdater.update(radioData ->
                radioData.comReceiveAll = receiveAll);
        }
    }

    @Override
    public void setCom1Standby(float frequency) {
        currentDelegate.setCom1Standby(frequency);
        if (radioUpdater != null) {
            radioUpdater.update(radioData ->
                radioData.com1standby = frequency);
        }
    }

    @Override
    public void setNav1Standby(float frequency) {
        currentDelegate.setNav1Standby(frequency);
        if (radioUpdater != null) {
            radioUpdater.update(radioData ->
                radioData.nav1standby = frequency);
        }
    }

    ConnectionDelegate initConnectionFromConfig(ConnectionConfiguration config) {
        Timber.v("init(type=%s, host=%s, port=%d)", config.type, config.host, config.port);
        switch (config.type) {
        default:
            Timber.e("Unexpected config type %s", config.type);
        case NONE:
            return new NullConnection();

        case DUMMY:
            return new DummyConnection();

        case SIM_CONNECT:
            return new SimConnectConnection(config.host, config.port);
        }
    }

    static class LocalDataMerger<T> {
        /** wait plenty of time for the change to make it */
        static final long CHANGE_WAIT_TIME_MS = 2500;

        final Observable<T> baseStream;
        T lastData;
        PublishRelay<T> localChanges = PublishRelay.create();
        long changePending;

        LocalDataMerger(Observable<T> baseStream) {
            this.baseStream = baseStream
                .filter(any -> System.currentTimeMillis() >= changePending)
                .share();

            this.baseStream.subscribe(updated -> {
                lastData = updated;
            });
        }

        Observable<T> merged() {
            return Observable.merge(
                baseStream,
                localChanges
            );
        }

        public void update(Action1<T> updater) {
            if (lastData != null) {
                changePending = System.currentTimeMillis() + CHANGE_WAIT_TIME_MS;
                updater.call(lastData);
                localChanges.call(lastData);
            }
        }

        public static <T> LocalDataMerger<T> wrap(Observable<T> baseStream) {
            return new LocalDataMerger<>(baseStream);
        }
    }
}
