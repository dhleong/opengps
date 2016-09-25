package net.dhleong.opengps.ui.behavior;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Keep;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import net.dhleong.opengps.R;

/**
 * @author dhleong
 */
@Keep
public class BelowAppBarBehavior extends CoordinatorLayout.Behavior<View> {

    int targetId = View.NO_ID;

    @Keep
    public BelowAppBarBehavior(Context context, AttributeSet attr) {
        if (attr != null) {
            TypedArray a = context.obtainStyledAttributes(attr, R.styleable.BelowAppBarBehavior);
            targetId = a.getResourceId(R.styleable.BelowAppBarBehavior_belowId, targetId);
            a.recycle();
        }
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        if (targetId != View.NO_ID) {
            return dependency.getId() == targetId;
        }

        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
        params.topMargin = dependency.getTop()
            + (dependency.getVisibility() == View.GONE
                ? 0
                : dependency.getMeasuredHeight());
        child.setLayoutParams(params);
        child.requestLayout();
        return true;
    }
}
