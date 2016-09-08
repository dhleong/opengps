package net.dhleong.opengps.feat.fpl;

import android.content.Context;

import net.dhleong.opengps.GpsRoute;
import net.dhleong.opengps.OpenGps;
import net.dhleong.opengps.R;
import net.dhleong.opengps.connection.ConnectionDelegate;
import net.dhleong.opengps.feat.airway.AirwaySearchView;
import net.dhleong.opengps.feat.waypoint.WaypointSearchView;
import net.dhleong.opengps.ui.DialogPrompter;
import net.dhleong.opengps.ui.NavigateUtil;
import net.dhleong.opengps.util.BasePresenter;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import timber.log.Timber;

import static net.dhleong.opengps.util.RxUtil.notNull;

/**
 * @author dhleong
 */
public class FlightPlannerPresenter extends BasePresenter<FlightPlannerView> {

    @Inject Context context;
    @Inject OpenGps gps;

    @Inject GpsRoute route;
    @Inject Action1<GpsRoute> routeUpdater;
    @Inject ConnectionDelegate connection;

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
                .filter(notNull())
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
                    NavigateUtil.intoWaypoint(context, waypoint);
                })
        );

        subscribe(
            view.tuneNavaidEvents()
                .subscribe(waypoint -> {
                    connection.setNav1Standby((float) waypoint.freq());
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
