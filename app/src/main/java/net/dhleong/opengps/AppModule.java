package net.dhleong.opengps;

import android.content.Context;

import net.dhleong.opengps.nasr.NasrTextDataSource;
import net.dhleong.opengps.storage.InMemoryStorage;
import net.dhleong.opengps.util.scopes.Root;

import java.io.File;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import timber.log.Timber;

/**
 * @author dhleong
 */
@Module
public class AppModule {

    private final Context appContext;

    AppModule(App context) {
        this.appContext = context;
    }

    @Provides @Root Context context() {
        return appContext;
    }

    @Provides @Singleton OpenGps gps(@Root Context context) {

        final File nasrZipFile = new File(context.getCacheDir(), "nasr-data.zip");

        return new OpenGps.Builder()
            .storage(new InMemoryStorage())
            .addDataSource(new NasrTextDataSource(nasrZipFile))
            .onError(e -> {
                Timber.e("Error loading data", e);
                // TODO snackbar?
            })
            .build();
    }
}
