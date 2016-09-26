package net.dhleong.opengps;

import android.content.Context;

import com.jakewharton.rxrelay.PublishRelay;

import net.dhleong.opengps.faa.FaaChartsSource;
import net.dhleong.opengps.faa.FaaChartsStorage;
import net.dhleong.opengps.modules.ConnectionModule;
import net.dhleong.opengps.modules.NetworkModule;
import net.dhleong.opengps.modules.PrefsModule;
import net.dhleong.opengps.nasr.NasrPreferredRoutesStorage;
import net.dhleong.opengps.nasr.NasrTextDataSource;
import net.dhleong.opengps.storage.DelegateStorage;
import net.dhleong.opengps.storage.InMemoryStorage;
import net.dhleong.opengps.util.scopes.Root;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import rx.Observable;
import rx.functions.Action1;

/**
 * @author dhleong
 */
@Module(includes = {
    ConnectionModule.class,
    NetworkModule.class,
    PrefsModule.class
})
public class AppModule {

    private final Context appContext;

    private final AtomicReference<GpsRoute> route = new AtomicReference<>(new GpsRoute());
    private final PublishRelay<GpsRoute> routeUpdates = PublishRelay.create();

    AppModule(App context) {
        this.appContext = context;

        routeUpdates.subscribe(newRoute -> {
            route.set(newRoute.copy());
        });
    }

    @Provides @Root Context context() {
        return appContext;
    }

    @Provides @Singleton OpenGps gps(@Root Context context) {

        File nasrCacheDir = new File(context.getCacheDir(), "nasr");
        File chartsCacheDir = new File(context.getCacheDir(), "charts");

        return new OpenGps.Builder()
            .storage(
                new DelegateStorage.Builder()
                    .add(new InMemoryStorage())
                    .add(new FaaChartsStorage())
                    .add(new NasrPreferredRoutesStorage())
                    .build()
            )
            .addDataSource(new NasrTextDataSource(nasrCacheDir))
            .addDataSource(new FaaChartsSource(chartsCacheDir))
            .dontThrowOnError() // we'll handle in LoadingStatusView
            .build();
    }


    @Provides AtomicReference<GpsRoute> routeRef() {
        return route;
    }

    @Provides GpsRoute route() {
        return route.get();
    }

    @Provides Action1<GpsRoute> routeUpdater() {
        return routeUpdates;
    }

    @Provides Observable<GpsRoute> routeUpdates() {
        return routeUpdates;
    }
}
