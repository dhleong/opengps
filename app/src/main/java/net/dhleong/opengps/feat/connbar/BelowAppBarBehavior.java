package net.dhleong.opengps.feat.connbar;

import android.content.Context;
import android.support.annotation.Keep;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author dhleong
 */
@Keep
public class BelowAppBarBehavior extends CoordinatorLayout.Behavior<View> {

    @Keep
    public BelowAppBarBehavior(Context context, AttributeSet attr) {
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
        params.topMargin = dependency.getMeasuredHeight();
        return true;
    }
}
