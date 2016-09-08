package net.dhleong.opengps.feat.map;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import net.dhleong.opengps.App;
import net.dhleong.opengps.R;
import net.dhleong.opengps.ui.LifecycleDelegate;
import net.dhleong.opengps.util.LatLngHdg;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Named as such to avoid confusion with Google's MapView
 * @author dhleong
 */
public class MapFeatureView
        extends FrameLayout
        implements LifecycleDelegate, OnMapReadyCallback {

    private static final float MIN_ZOOM_LEVEL = 7f;
    private static final float MAX_ZOOM_LEVEL = 16f;
    private static final float DEFAULT_ZOOM_LEVEL = 14f;
    
    @Inject Observable<LatLngHdg> gpsUpdates;

    @BindView(R.id.map) MapView mapView;

    CompositeSubscription subs = new CompositeSubscription();
    GoogleMap map;
    boolean isFirst = true;

    public MapFeatureView(Context context) {
        super(context);
    }

    public MapFeatureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MapFeatureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        App.activityComponent(this)
           .newMapComponent()
           .inject(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mapView.getMapAsync(this);

        subs.add(
            gpsUpdates.observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::onGpsUpdate)
        );
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        subs.clear();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mapView.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        mapView.onResume();
    }

    @Override
    public void onPause() {
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (!isAttachedToWindow()) return;

        Timber.v("MAP READY");
        this.map = googleMap;
        map.setIndoorEnabled(false);
        map.setMaxZoomPreference(MAX_ZOOM_LEVEL);
        map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        UiSettings settings = map.getUiSettings();
        settings.setCompassEnabled(false);
    }

    void onGpsUpdate(LatLngHdg update) {
        final GoogleMap map = this.map;
        if (map == null) return;

        // wait to disable UI gestures until we actually have some location
        UiSettings settings = map.getUiSettings();
        settings.setAllGesturesEnabled(false);
        settings.setZoomControlsEnabled(true);
        settings.setZoomGesturesEnabled(true);

        // also, let them stay zoomed out until now
        // TODO maybe remember the last location and be able to zoom there
        map.setMinZoomPreference(MIN_ZOOM_LEVEL);

        CameraPosition.Builder builder =
            new CameraPosition.Builder()
                .target(new LatLng(update.lat, update.lng))
                .bearing(update.hdg);

        if (isFirst) {
            isFirst = false;
            builder.zoom(DEFAULT_ZOOM_LEVEL);
            Timber.v("GOT LOCATION!");
        } else {
            builder.zoom(map.getCameraPosition().zoom);
        }

        map.moveCamera(CameraUpdateFactory.newCameraPosition(
            builder.build()
        ));
    }
}
