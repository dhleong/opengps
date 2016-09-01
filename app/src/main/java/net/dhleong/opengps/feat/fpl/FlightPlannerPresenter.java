package net.dhleong.opengps.feat.fpl;

import android.content.Context;

import net.dhleong.opengps.Airport;
import net.dhleong.opengps.GpsRoute;
import net.dhleong.opengps.NavFix;
import net.dhleong.opengps.Navaid;
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

    GpsRoute route = new GpsRoute();

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
            view.removeWaypointEvents()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(waypoint -> {
                    int idx = route.indexOfWaypoint(waypoint);
                    route.removeStep(idx);
                    Timber.v("Route = %s", route);
                    view.setRoute(route);
                })
        );

        subscribe(
            view.removeAfterWaypointEvents()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(waypoint -> {
                    int idx = route.indexOfWaypoint(waypoint);
                    route.removeStepsAfter(idx);
                    Timber.v("Route = %s", route);
                    view.setRoute(route);
                })
        );

        subscribe(
            view.viewWaypointEvents()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(waypoint -> {
                    if (waypoint instanceof Airport) {
                        Timber.v("TODO view airport");
                    } else if (waypoint instanceof Navaid) {
                        Timber.v("TODO view navaid");
                    } else if (waypoint instanceof NavFix) {
                        Timber.v("TODO view navfix");
                    }
                })
        );

        subscribe(
            view.loadAirwayEvents()
                .flatMap(airwayStart ->
                    DialogPrompter.prompt(context, AirwaySearchView.class, R.layout.feat_airway, airwayStart))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    // NB: index + 1 because we're inserting *after* the waypoint
                    final int index = route.indexOfWaypoint(result.entry);
                    result.airway.appendPointsBetween(result.entry, result.exit, route, index + 1);
                    view.setRoute(route);
                })
        );
    }
}
