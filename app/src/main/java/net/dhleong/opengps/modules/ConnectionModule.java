package net.dhleong.opengps.modules;

import net.dhleong.opengps.connection.ConnectionConfiguration;
import net.dhleong.opengps.connection.ConnectionDelegate;
import net.dhleong.opengps.connection.SimConnectConnection;
import net.dhleong.opengps.util.LatLngHdg;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import rx.Observable;
import timber.log.Timber;

/**
 * @author dhleong
 */
@Module
public class ConnectionModule {

    ConnectionDelegate previousDelegate;

    @Provides @Singleton Observable<ConnectionDelegate> delegate(
            Observable<ConnectionConfiguration> configs) {
        return configs.map(config -> {
            final ConnectionDelegate old = previousDelegate;
            if (old != null) {
                old.close();
            }

            switch (config.type) {
            default:
                Timber.e("Unexpected config type %s", config.type);
            case NONE:
                return new DummyConnection();

            case SIM_CONNECT:
                return new SimConnectConnection(config.host, config.port);
            }
        }).doOnNext(conn -> {
            // save and open
            previousDelegate = conn;
            conn.open();
        });
    }

    @Provides Observable<LatLngHdg> latLngHdg(Observable<ConnectionDelegate> delegateObs) {
        return delegateObs.flatMap(delegate -> delegate.subscribe(LatLngHdg.class));
    }
}
