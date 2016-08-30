package net.dhleong.opengps.feat.waypoint;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;

import net.dhleong.opengps.AeroObject;
import net.dhleong.opengps.App;
import net.dhleong.opengps.OpenGps;
import net.dhleong.opengps.ui.DialogPrompter;

import javax.inject.Inject;

import butterknife.ButterKnife;
import rx.Single;

/**
 * @author dhleong
 */
public class WaypointSearchView
        extends CoordinatorLayout
        implements DialogPrompter.PrompterView<Void, AeroObject> {

    @Inject OpenGps gps;

    public WaypointSearchView(Context context) {
        super(context);
    }

    public WaypointSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WaypointSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        App.component(getContext())
           .newWaypointSearchComponent()
           .inject(this);
    }

    @Override
    public Single<AeroObject> result(Void ignore) {
//        return Observable.<AeroObject>empty().toSingle();
        // TODO for real
        return gps.find("BDR").first().toSingle();
    }
}
