package net.dhleong.opengps.activities;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.dhleong.opengps.R;
import net.dhleong.opengps.core.ActivityModuleActivity;
import net.dhleong.opengps.feat.connbar.ConnectionAppBarLayout;
import net.dhleong.opengps.ui.LifecycleDelegate;
import net.dhleong.opengps.ui.NavigateUtil;
import net.dhleong.opengps.ui.UiUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity
        extends ActivityModuleActivity
        implements NavigateUtil.IntoNavigator {

    @BindView(R.id.appbar) ConnectionAppBarLayout appBarLayout;
    @BindView(R.id.activity_main) ViewGroup container;

    private View intoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
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

        // hide keyboard first, since if it was requested
        //  it would have been from the current view
        UiUtil.hideKeyboard(this);

        final View prev = container.getChildAt(0);
        appBarLayout.intercept(prev, view);
        container.removeView(prev);
        container.addView(view);
        intoView = view;

        if (view instanceof LifecycleDelegate) {
            ((LifecycleDelegate) view).onCreate(null);
            ((LifecycleDelegate) view).onResume();
        }

        if (prev instanceof LifecycleDelegate) {
            ((LifecycleDelegate) prev).onPause();
            ((LifecycleDelegate) prev).onDestroy();
        }

        return prev;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (intoView instanceof LifecycleDelegate) {
            ((LifecycleDelegate) intoView).onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (intoView instanceof LifecycleDelegate) {
            ((LifecycleDelegate) intoView).onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (intoView instanceof LifecycleDelegate) {
            ((LifecycleDelegate) intoView).onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        if (intoView instanceof LifecycleDelegate) {
            ((LifecycleDelegate) intoView).onLowMemory();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (intoView instanceof LifecycleDelegate) {
            ((LifecycleDelegate) intoView).onSaveInstanceState(outState);
        }
    }

    @Override
    public View inflate(@LayoutRes int layoutResId) {
        return LayoutInflater.from(this).inflate(layoutResId, container, false);
    }

    @Override
    public View current() {
        return container.getChildAt(0);
    }
}
