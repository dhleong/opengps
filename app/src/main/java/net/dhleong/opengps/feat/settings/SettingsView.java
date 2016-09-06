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

import net.dhleong.opengps.R;
import net.dhleong.opengps.modules.PrefsModule;

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

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs_main);
            PreferenceManager man = getPreferenceManager();

            final Preference typePref =
                man.findPreference(PrefsModule.PREF_CONNECTION_TYPE);
            final Preference hostPref =
                man.findPreference(PrefsModule.PREF_CONNECTION_HOST);
            final Preference portPref =
                man.findPreference(PrefsModule.PREF_CONNECTION_PORT);

            if (((ListPreference) typePref).getValue().equals("NONE")) {
                hostPref.setEnabled(false);
                portPref.setEnabled(false);
            }
            typePref.setOnPreferenceChangeListener((preference, newValue) -> {
                   boolean enabled = !newValue.equals("NONE");
                   hostPref.setEnabled(enabled);
                   portPref.setEnabled(enabled);
                   return true; // allow change
               });

            // show current value as summary where appropriate
            summarify(typePref);
            summarify(hostPref);
            summarify(portPref);
        }

        static void summarify(Preference pref) {
            pref.setSummary(summaryFor(pref));
            Preference.OnPreferenceChangeListener existingListener =
                pref.getOnPreferenceChangeListener();

            pref.setOnPreferenceChangeListener((preference, newValue) -> {
                if (existingListener != null &&
                        !existingListener.onPreferenceChange(preference, newValue)) {
                    return false;
                }

                pref.setSummary(summaryFor(pref));
                return true;
            });
        }

        static CharSequence summaryFor(Preference pref) {
            if (pref instanceof EditTextPreference) {
                return ((EditTextPreference) pref).getText();
            }
            if (pref instanceof ListPreference) {
                return ((ListPreference) pref).getEntry();
            }

            return pref.getSummary();
        }

        static PrefsFragment newInstance() {
            return new PrefsFragment();
        }
    }
}
