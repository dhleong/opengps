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
             currentDelegate = conn;
             conn.open();
        });
    }

    @Override
    public void close() {
        final ConnectionDelegate delegate = currentDelegate;
        currentDelegate = null;
        if (delegate != null) {
            delegate.close();
        }

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
    public <T> Observable<T> subscribe(Class<T> type) {
        return connection.flatMap(conn -> conn.subscribe(type));
    }

    ConnectionDelegate initConnectionFromConfig(ConnectionConfiguration config) {
        Timber.v("init(type=%s, host=%s, port=%d)", config.type, config.host, config.port);
        switch (config.type) {
        default:
            Timber.e("Unexpected config type %s", config.type);
        case NONE:
            return new DummyConnection();

        case SIM_CONNECT:
            return new SimConnectConnection(config.host, config.port);
        }
    }

}
