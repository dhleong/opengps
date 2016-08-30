package net.dhleong.opengps.feat.fpl;

import android.content.Context;

import net.dhleong.opengps.GpsRoute;
import net.dhleong.opengps.OpenGps;
import net.dhleong.opengps.R;
import net.dhleong.opengps.feat.airway.AirwaySearchView;
import net.dhleong.opengps.feat.waypoint.WaypointSearchView;
import net.dhleong.opengps.ui.DialogPrompter;
import net.dhleong.opengps.util.BasePresenter;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

/**
 * @author dhleong
 */
public class FlightPlannerPresenter extends BasePresenter<FlightPlannerView> {

    @Inject Context context;
    @Inject OpenGps gps;

    GpsRoute route = new GpsRoute(0); // don't include fixes for now

    @Inject FlightPlannerPresenter() {}

    @Override
    public void onViewAttached(FlightPlannerView view) {
        subscribe(
            view.addWaypointEvents()
                .flatMap(request ->
                    DialogPrompter.prompt(context, WaypointSearchView.class, R.layout.feat_waypoint, null))
                .doOnNext(route::add)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(any -> {
                    Timber.v("Route = %s", route);
                    view.setRoute(route);
                })
        );

        subscribe(
            view.loadAirwayEvents()
                .flatMap(airwayStart ->
                    DialogPrompter.prompt(context, AirwaySearchView.class, R.layout.feat_airway, airwayStart))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    final int index = route.indexOfWaypoint(result.entry);
                    result.airway.appendPointsBetween(result.entry, result.exit, route, index);
                    view.setRoute(route);
                })
        );
    }
}
