package net.dhleong.opengps.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.dhleong.opengps.AeroObject;
import net.dhleong.opengps.App;
import net.dhleong.opengps.R;
import net.dhleong.opengps.impl.BaseAeroObject;
import net.dhleong.opengps.util.LatLngHdg;
import net.dhleong.opengps.views.BearingIndicatorView;

import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * @author dhleong
 */
public class WaypointHeaderView extends RelativeLayout {

    @Inject Observable<LatLngHdg> positionObservable;

    @BindView(R.id.id) TextView id;
    @BindView(R.id.name) TextView name;
    @BindView(R.id.id_name_spacer) View spacer;

    @BindView(R.id.bearing) TextView bearing;
    @BindView(R.id.bearing_indicator) BearingIndicatorView bearingIndicator;
    @BindView(R.id.distance) TextView distance;

    @BindView(R.id.lat) TextView lat;
    @BindView(R.id.lng) TextView lng;

    CompositeSubscription subs = new CompositeSubscription();
    private DummyObject locationObject = new DummyObject();
    private AeroObject object;

    public WaypointHeaderView(Context context) {
        super(context);
    }

    public WaypointHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WaypointHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);

        App.activityComponent(this)
           .inject(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        bearing.setText("---");
        distance.setText("---");

        if (!isInEditMode()) {
            subs.add(
                positionObservable.observeOn(AndroidSchedulers.mainThread())
                                  .subscribe(this::onPositionUpdate)
            );
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        subs.clear();
    }

    public void bind(AeroObject object) {
        this.object = object;
        final String objId = object.id();
        final String objName = object.name();
        id.setText(objId);

        // handle spacing for navfixes, whose name is the same as their id
        if (objId.equals(objName)) {
            final LayoutParams params = (LayoutParams) spacer.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_END, R.id.id);
        } else {
            name.setText(objName);
        }

        lat.setText(TextUtil.formatLat(object.lat()));
        lng.setText(TextUtil.formatLng(object.lng()));
    }

    @SuppressLint("SetTextI18n")
    void onPositionUpdate(LatLngHdg update) {
        AeroObject obj = this.object;
        if (obj == null) return;

        locationObject.lat = update.lat;
        locationObject.lng = update.lng;
        final float bearingTo = locationObject.bearingTo(obj);
        bearing.setText(String.format(Locale.US, "%03.0f\u00b0", bearingTo));
        bearingIndicator.setBearing(bearingTo);

        double dist = locationObject.distanceTo(obj);
        if (dist > 99) {
            distance.setText(">99 NM");
        } else {
            distance.setText(String.format(Locale.US, "%.0f NM", dist));
        }
    }

    static class DummyObject extends BaseAeroObject {
        double lat, lng;

        public DummyObject() {
            super(null, null, 0, 0);
        }

        @Override
        public String id() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String name() {
            throw new UnsupportedOperationException();
        }

        @Override
        public double lat() {
            return lat;
        }

        @Override
        public double lng() {
            return lng;
        }
    }
}
