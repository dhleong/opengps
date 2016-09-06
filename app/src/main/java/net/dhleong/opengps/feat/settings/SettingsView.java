package net.dhleong.opengps.feat.settings;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import net.dhleong.opengps.App;
import net.dhleong.opengps.R;
import net.dhleong.opengps.connection.ConnectionType;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static net.dhleong.opengps.modules.PrefsModule.PREF_CONNECTION_HOST;
import static net.dhleong.opengps.modules.PrefsModule.PREF_CONNECTION_PORT;
import static net.dhleong.opengps.modules.PrefsModule.PREF_CONNECTION_TYPE;

/**
 * @author dhleong
 */
public class SettingsView extends FrameLayout {

    public SettingsView(Context context) {
        super(context);
    }

    public SettingsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SettingsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // hacks? hacks.
        Activity act = (Activity) getContext();
        act.getFragmentManager()
           .beginTransaction()
           .replace(getId(), PrefsFragment.newInstance())
           .commit();
    }

    public static class PrefsFragment extends PreferenceFragment {

        @Inject Observable<ConnectionType> connectionTypePref;

        CompositeSubscription subs = new CompositeSubscription();

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs_main);
            App.component(getActivity())
               .inject(this);

            PreferenceManager man = getPreferenceManager();
            final Preference typePref =
                man.findPreference(PREF_CONNECTION_TYPE);
            final Preference hostPref =
                man.findPreference(PREF_CONNECTION_HOST);
            final Preference portPref =
                man.findPreference(PREF_CONNECTION_PORT);

            subs.add(
                connectionTypePref.asObservable()
                                  .observeOn(AndroidSchedulers.mainThread())
                                  .subscribe(type -> {
                                      boolean enabled = type != ConnectionType.NONE;
                                      Timber.v("Enabled = %s (%s)", enabled, type);
                                      hostPref.setEnabled(enabled);
                                      portPref.setEnabled(enabled);
                                  })
            );

            // TODO validate port/host

            // show current value as summary where appropriate
            summarify(typePref);
            summarify(hostPref);
            summarify(portPref);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            subs.clear();
        }

        static void summarify(Preference pref) {
            pref.setSummary(summaryFor(pref, null));

            final Preference.OnPreferenceChangeListener existingListener =
                pref.getOnPreferenceChangeListener();

            pref.setOnPreferenceChangeListener((preference, newValue) -> {
                if (existingListener != null &&
                        !existingListener.onPreferenceChange(preference, newValue)) {
                    return false;
                }

                pref.setSummary(summaryFor(pref, newValue));
                return !(preference instanceof ListPreference);
            });
        }

        static CharSequence summaryFor(Preference pref, Object newValue) {
            if (pref instanceof EditTextPreference) {
                if (newValue != null) return (CharSequence) newValue;
                return ((EditTextPreference) pref).getText();
            }
            if (pref instanceof ListPreference) {
                ListPreference list = (ListPreference) pref;
                if (newValue != null) {
                    list.setValue((String) newValue);
                }
                return list.getEntry();
            }

            return pref.getSummary();
        }

        static PrefsFragment newInstance() {
            return new PrefsFragment();
        }
    }
}
