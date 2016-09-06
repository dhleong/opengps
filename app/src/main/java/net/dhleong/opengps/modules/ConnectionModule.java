package net.dhleong.opengps.modules;

import net.dhleong.opengps.connection.ConnectionDelegate;
import net.dhleong.opengps.connection.data.RadioData;
import net.dhleong.opengps.util.LatLngHdg;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import rx.Observable;

/**
 * @author dhleong
 */
@Module
public abstract class ConnectionModule {

    @Binds @Singleton abstract ConnectionDelegate delegate(RxChangingConnectionDelegate impl);

    @Provides static Observable<LatLngHdg> latLngHdg(ConnectionDelegate delegate) {
        return delegate.subscribe(LatLngHdg.class);
    }

    @Provides static Observable<RadioData> radios(ConnectionDelegate delegate) {
        return delegate.subscribe(RadioData.class);
    }
}
