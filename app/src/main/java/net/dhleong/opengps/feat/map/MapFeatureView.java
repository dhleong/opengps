package net.dhleong.opengps.feat.map;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

import net.dhleong.opengps.R;
import net.dhleong.opengps.ui.LifecycleDelegate;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Named as such to avoid confusion with Google's MapView
 * @author dhleong
 */
public class MapFeatureView
        extends FrameLayout
        implements LifecycleDelegate, OnMapReadyCallback {

    @BindView(R.id.map) MapView mapView;

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
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mapView.getMapAsync(this);
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
        Timber.v("MAP READY");
    }
}
