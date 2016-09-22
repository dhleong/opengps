package net.dhleong.opengps.feat.routeinfo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;

import net.dhleong.opengps.PreferredRoute;
import net.dhleong.opengps.R;
import net.dhleong.opengps.ui.DialogPrompter;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Single;

/**
 * @author dhleong
 */
public class RouteInfoView
        extends LinearLayout
        implements DialogPrompter.PrompterView<PreferredRoute, PreferredRoute> {

    @BindView(R.id.area) TextView area;
    @BindView(R.id.aircraft) TextView aircraft;
    @BindView(R.id.altitude) TextView altitude;
    @BindView(R.id.direction) TextView direction;
    @BindView(R.id.route) TextView route;
    @BindView(R.id.confirm) View confirm;

    public RouteInfoView(Context context) {
        super(context);
    }

    public RouteInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RouteInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    @Override
    public Single<PreferredRoute> result(PreferredRoute input) {

        area.setText(input.area);
        aircraft.setText(input.aircraftLimitations);
        altitude.setText(input.altitude);
        direction.setText(input.direction);
        route.setText(input.routeString);

        return RxView.clicks(confirm)
                     .map(any -> input)
                     .first()
                     .toSingle();
    }
}
