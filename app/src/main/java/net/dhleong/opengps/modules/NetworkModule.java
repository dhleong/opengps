package net.dhleong.opengps.modules;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.dhleong.opengps.util.scopes.Root;

import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;
import timber.log.Timber;

/**
 * @author dhleong
 */
@Module
public class NetworkModule {

    /** 50 mb (for pdfs) */
    private static final long CACHE_SIZE = 50 * 1024 * 1024;

    @Provides @Singleton OkHttpClient okhttp(@Root Context context) {
        return new OkHttpClient.Builder()
            .cache(new Cache(context.getCacheDir(), CACHE_SIZE))
            .addInterceptor(chain -> {
                final Request originalRequest = chain.request();
                if (!"GET".equals(originalRequest.method())
                        || originalRequest.url().host().contains("weather")) {
                    // don't cache if not @GET, or if it's a wx request
                    return chain.proceed(originalRequest);
                }

                final CacheControl cacheControl;
                final Response originalResponse;
                if (originalRequest.cacheControl().maxAgeSeconds() > TimeUnit.DAYS.toSeconds(7)) {
                    // NB: already has a cache time longer than 7 days
                    originalResponse = chain.proceed(originalRequest);
                    cacheControl = originalRequest.cacheControl();
                } else {

                    // NB: this cache policy may not be ideal; it's conceivable
                    //  that we could cache some charts right before they become
                    //  invalid by a new airac cycle, for example. But, let's be lazy
                    //  for now;
                    //  it's also likely that the charts won't change much between
                    //  cycles... right?
                    cacheControl = new CacheControl.Builder()
                        .maxAge(7, TimeUnit.DAYS)
                        .build();
                    originalResponse = chain.proceed(
                        originalRequest.newBuilder()
                                       .cacheControl(cacheControl)
                                       .build()
                    );
                }

                if (null != originalResponse.cacheResponse()) {
                    Timber.v("Served %s from cache!", originalRequest);
                } else {
                    Timber.v("Load %s fresh!", originalRequest);
                }
                if (originalResponse.header("Cache-Control") == null) {
                    // force cached
                    return originalResponse.newBuilder()
                        .addHeader("Cache-Control", cacheControl.toString())
                        .build();
                } else {
                    return originalResponse;
                }
            })
            .build();
    }

    @Provides @Singleton Gson gson() {
        return new GsonBuilder().create();
    }

    @Provides Retrofit.Builder retrofitBuilder(OkHttpClient okhttp, Gson gson) {
        return new Retrofit.Builder()
            .client(okhttp)
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create());
    }

    @Provides @Singleton @Named("charts") Retrofit chartsRetrofit(Retrofit.Builder builder) {
        return builder.baseUrl("http://api.aircharts.org").build();
    }

    @Provides @Singleton @Named("wx") Retrofit weatherRetrofit(Retrofit.Builder builder) {
        return builder.baseUrl("http://www.aviationweather.gov/adds/dataserver_current/").build();
    }

}
