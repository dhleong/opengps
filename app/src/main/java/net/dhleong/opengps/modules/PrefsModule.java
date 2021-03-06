package net.dhleong.opengps.modules;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import net.dhleong.opengps.connection.ConnectionConfiguration;
import net.dhleong.opengps.connection.ConnectionType;
import net.dhleong.opengps.util.scopes.Root;

import java.util.concurrent.TimeUnit;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import rx.Observable;

import static net.dhleong.opengps.util.RxUtil.toVoid;

/**
 * @author dhleong
 */
@Module
public class PrefsModule {

    public static final String PREF_KEEP_SCREEN = "pref_main_screen";
    public static final String PREF_CONNECTION_TYPE = "pref_connection_type";
    public static final String PREF_CONNECTION_HOST = "pref_connection_host";
    public static final String PREF_CONNECTION_PORT = "pref_connection_port";

    @Provides SharedPreferences defaultSharedPrefs(@Root Context context) {
        return get(context);
    }

    @Provides RxSharedPreferences rxPrefs(SharedPreferences prefs) {
        return RxSharedPreferences.create(prefs);
    }

    @Provides @Named(PREF_KEEP_SCREEN) Preference<Boolean> keepScreen(RxSharedPreferences prefs) {
        return prefs.getBoolean(PREF_KEEP_SCREEN, false);
    }

    @Provides @Named(PREF_CONNECTION_TYPE) Preference<ConnectionType> connectionType(
            RxSharedPreferences prefs) {
        return prefs.getEnum(PREF_CONNECTION_TYPE, ConnectionType.NONE, ConnectionType.class);
    }

    @Provides Observable<ConnectionType> connectionTypeObs(
            @Named(PREF_CONNECTION_TYPE) Preference<ConnectionType> pref) {
        return pref.asObservable();
    }

    @Provides @Named(PREF_CONNECTION_HOST) Preference<String> connectionHost(
            RxSharedPreferences prefs) {
        return prefs.getString(PREF_CONNECTION_HOST);
    }

    @Provides @Named(PREF_CONNECTION_PORT) Preference<Integer> connectionPort(
            RxSharedPreferences prefs) {
        return prefs.getObject(PREF_CONNECTION_PORT, 4567, new Preference.Adapter<Integer>() {
            @Override
            public Integer get(@NonNull String key, @NonNull SharedPreferences preferences) {
                try {
                    return Integer.parseInt(preferences.getString(key, null));
                } catch (Throwable e) {
                    return 4567;
                }
            }

            @Override
            public void set(@NonNull String key, @NonNull Integer value, @NonNull SharedPreferences.Editor editor) {
                editor.putString(key, value.toString());
            }
        });
    }

    @Provides Observable<ConnectionConfiguration> connectionConfig(
        @Named(PREF_CONNECTION_TYPE) Preference<ConnectionType> typePref,
        @Named(PREF_CONNECTION_HOST) Preference<String> hostPref,
        @Named(PREF_CONNECTION_PORT) Preference<Integer> portPref
    ) {
        //noinspection ConstantConditions
        return Observable.merge(
            typePref.asObservable().map(toVoid()),
            hostPref.asObservable().map(toVoid()),
            portPref.asObservable().map(toVoid())
        ).debounce(500, TimeUnit.MILLISECONDS)
         .map(any -> new ConnectionConfiguration(
            typePref.get(),
            hostPref.get(),
            portPref.get()
         )).share();
    }

    public static SharedPreferences get(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

}
