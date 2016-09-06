package net.dhleong.opengps.modules;

import net.dhleong.opengps.connection.ConnectionConfiguration;
import net.dhleong.opengps.connection.ConnectionDelegate;
import net.dhleong.opengps.connection.SimConnectConnection;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import timber.log.Timber;

/**
 * @author dhleong
 */
public class RxChangingConnectionDelegate implements ConnectionDelegate {
    private final Observable<ConnectionDelegate> connection;

    ConnectionDelegate currentDelegate;
    Subscription subs;

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
        }).share();
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
        subs = connection.subscribe();
    }

    @Override
    public Observable<State> state() {
        return connection.flatMap(conn ->
            conn.state()
                .takeWhile(any -> currentDelegate == conn));
    }

    @Override
    public <T> Observable<T> subscribe(Class<T> type) {
        return connection.flatMap(conn -> conn.subscribe(type));
    }

    @Override
    public void swapCom1() {
        currentDelegate.swapCom1();
    }

    @Override
    public void swapNav1() {
        currentDelegate.swapNav1();
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

}
