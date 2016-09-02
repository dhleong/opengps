package net.dhleong.opengps.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.dhleong.opengps.R;
import net.dhleong.opengps.core.ActivityModuleActivity;
import net.dhleong.opengps.ui.DialogPrompter;
import net.dhleong.opengps.ui.NavigateUtil;

import timber.log.Timber;

public class MainActivity
        extends ActivityModuleActivity
        implements NavigateUtil.IntoNavigator {

    private View intoView;
    private ViewGroup container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        container = (ViewGroup) findViewById(R.id.activity_main);
    }

    @Override
    public void onBackPressed() {
        if (intoView != null && NavigateUtil.backFrom(intoView)) {
            Timber.v("Back from %s", intoView);
            return;
        }

        Timber.v("No going back from %s", intoView);
        super.onBackPressed();
    }

    @Override
    public View navigateInto(View view) {
        Timber.v("navigateInto(%s)", view);
        final View prev = container.getChildAt(0);
        container.removeView(prev);
        container.addView(view);
        intoView = view;
        return prev;
    }

    @Override
    public <T, V extends DialogPrompter.PrompterView<T, ?>> V inflate(Class<V> viewClass, int layoutResId) {
        return viewClass.cast(LayoutInflater.from(this).inflate(layoutResId, container, false));
    }
}
