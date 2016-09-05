package net.dhleong.opengps.feat.settings;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import net.dhleong.opengps.R;

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
        }

        static PrefsFragment newInstance() {
            return new PrefsFragment();
        }
    }
}
