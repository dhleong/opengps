package net.dhleong.opengps.activities;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.dhleong.opengps.R;
import net.dhleong.opengps.core.ActivityModuleActivity;
import net.dhleong.opengps.feat.connbar.ConnectionAppBarLayout;
import net.dhleong.opengps.ui.NavigateUtil;

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
        final View prev = container.getChildAt(0);
        appBarLayout.intercept(prev, view);
        container.removeView(prev);
        container.addView(view);
        intoView = view;
        return prev;
    }

    @Override
    public View inflate(@LayoutRes int layoutResId) {
        return LayoutInflater.from(this).inflate(layoutResId, container, false);
    }
}
