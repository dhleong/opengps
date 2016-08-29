package net.dhleong.opengps;

import android.content.Context;

import net.dhleong.opengps.nasr.NasrTextDataSource;
import net.dhleong.opengps.storage.InMemoryStorage;

import java.io.File;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * @author dhleong
 */
@Module
public class AppModule {

    private final Context appContext;

    AppModule(App context) {
        this.appContext = context;
    }

    @Provides Context context() {
        return appContext;
    }

    @Provides @Singleton OpenGps gps(Context context) {

        final File nasrZipFile = new File(context.getCacheDir(), "nasr-data.zip");

        return new OpenGps.Builder()
            .storage(new InMemoryStorage())
            .addDataSource(new NasrTextDataSource(nasrZipFile))
            .build();
    }
}
