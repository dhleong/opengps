package net.dhleong.opengps.feat.fpl;

import android.content.Context;

import net.dhleong.opengps.Airport;
import net.dhleong.opengps.GpsRoute;
import net.dhleong.opengps.NavFix;
import net.dhleong.opengps.Navaid;
import net.dhleong.opengps.OpenGps;
import net.dhleong.opengps.R;
import net.dhleong.opengps.feat.airport.AirportInfoView;
import net.dhleong.opengps.feat.airway.AirwaySearchView;
import net.dhleong.opengps.feat.waypoint.WaypointSearchView;
import net.dhleong.opengps.ui.DialogPrompter;
import net.dhleong.opengps.ui.NavigateUtil;
import net.dhleong.opengps.util.BasePresenter;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import timber.log.Timber;

/**
 * @author dhleong
 */
public class FlightPlannerPresenter extends BasePresenter<FlightPlannerView> {

    @Inject Context context;
    @Inject OpenGps gps;

    @Inject GpsRoute route;
    @Inject Action1<GpsRoute> routeUpdater;

    @Inject FlightPlannerPresenter() {}

    @Override
    public void onViewAttached(FlightPlannerView view) {

        if (!route.isEmpty()) {
            // init with persisted route
            view.setRoute(route);
        }

        subscribe(
            view.addWaypointEvents()
                .flatMap(request ->
                    DialogPrompter.prompt(context, WaypointSearchView.class, R.layout.feat_waypoint, null))
                .doOnNext(route::add)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(any -> {
                    updateRoute(view, route);
                })
        );

        subscribe(
            view.removeWaypointEvents()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(waypoint -> {
                    int idx = route.indexOfWaypoint(waypoint);
                    route.removeStep(idx);
                    updateRoute(view, route);
                })
        );

        subscribe(
            view.removeAfterWaypointEvents()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(waypoint -> {
                    int idx = route.indexOfWaypoint(waypoint);
                    route.removeStepsAfter(idx);
                    updateRoute(view, route);
                })
        );

        subscribe(
            view.viewWaypointEvents()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(waypoint -> {
                    if (waypoint instanceof Airport) {
                        Timber.v("view airport");
//                        DialogPrompter.prompt(context, AirportInfoView.class,
//                            R.layout.feat_airport, (Airport) waypoint);
                        NavigateUtil.into(context, AirportInfoView.class,
                            R.layout.feat_airport, (Airport) waypoint);
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
                    updateRoute(view, route);
                })
        );
    }

    void updateRoute(FlightPlannerView view, GpsRoute route) {
        Timber.v("Route <- %s", route);
        view.setRoute(route);
        routeUpdater.call(route);
    }
}
