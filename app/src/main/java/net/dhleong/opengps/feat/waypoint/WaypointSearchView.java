package net.dhleong.opengps.feat.waypoint;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;

import net.dhleong.opengps.AeroObject;
import net.dhleong.opengps.App;
import net.dhleong.opengps.OpenGps;
import net.dhleong.opengps.R;
import net.dhleong.opengps.ui.DialogPrompter;

import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Single;

/**
 * @author dhleong
 */
public class WaypointSearchView
        extends CoordinatorLayout
        implements DialogPrompter.PrompterView<Void, AeroObject> {

    @Inject OpenGps gps;

    @BindView(R.id.input) EditText input;
    @BindView(R.id.confirm) TextView confirm;

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
        App.component(this)
           .newWaypointSearchComponent()
           .inject(this);
    }

    @Override
    public Single<AeroObject> result(Void ignore) {
//        return Observable.<AeroObject>empty().toSingle();
        // TODO for real
//        return gps.find("BDR").first().toSingle();
        return RxView.clicks(confirm)
                     .flatMap(any ->
                         gps.find(input.getText().toString().trim().toUpperCase(Locale.US)).first()
                     ).toSingle();
    }
}
