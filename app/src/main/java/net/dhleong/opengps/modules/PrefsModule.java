package net.dhleong.opengps.modules;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import net.dhleong.opengps.util.scopes.Root;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

/**
 * @author dhleong
 */
@Module
public class PrefsModule {

    public static final String PREF_KEEP_SCREEN = "pref_main_screen";

    @Provides SharedPreferences defaultSharedPrefs(@Root Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Provides RxSharedPreferences rxPrefs(SharedPreferences prefs) {
        return RxSharedPreferences.create(prefs);
    }

    @Provides @Named(PREF_KEEP_SCREEN) Preference<Boolean> keepScreen(RxSharedPreferences prefs) {
        return prefs.getBoolean(PREF_KEEP_SCREEN, false);
    }

}
