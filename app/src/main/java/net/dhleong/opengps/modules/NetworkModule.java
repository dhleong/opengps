package net.dhleong.opengps.modules;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.dhleong.opengps.util.scopes.Root;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author dhleong
 */
@Module
public class NetworkModule {

    /** 5 mb */
    private static final long CACHE_SIZE = 5 * 1024 * 1024;

    @Provides @Singleton OkHttpClient okhttp(@Root Context context) {
        return new OkHttpClient.Builder()
            .cache(new Cache(context.getCacheDir(), CACHE_SIZE))
            .build();
    }

    @Provides @Singleton Gson gson() {
        return new GsonBuilder().create();
    }

    @Provides Retrofit.Builder retrofitBuilder(OkHttpClient okhttp, Gson gson) {
        return new Retrofit.Builder()
            .client(okhttp)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create());
    }

    @Provides @Singleton @Named("charts") Retrofit chartsRetrofit(Retrofit.Builder builder) {
        return builder.baseUrl("http://api.aircharts.org").build();
    }

}
