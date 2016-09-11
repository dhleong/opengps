package net.dhleong.opengps.feat.navfix;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;

import net.dhleong.opengps.NavFix;
import net.dhleong.opengps.R;
import net.dhleong.opengps.ui.DialogPrompter;
import net.dhleong.opengps.ui.WaypointHeaderView;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Single;
import rx.subjects.PublishSubject;

/**
 * @author dhleong
 */
public class NavFixInfoView
        extends CoordinatorLayout
        implements DialogPrompter.PrompterView<NavFix, Void> {

    @BindView(R.id.waypoint) WaypointHeaderView headerView;

    public NavFixInfoView(Context context) {
        super(context);
    }

    public NavFixInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NavFixInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    @Override
    public Single<Void> result(NavFix input) {

        headerView.bind(input);

        // just something that never auto-completes
        return PublishSubject.<Void>create().toSingle();
    }
}
