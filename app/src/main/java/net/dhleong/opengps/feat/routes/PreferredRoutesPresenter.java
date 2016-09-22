package net.dhleong.opengps.feat.routes;

import android.content.Context;

import net.dhleong.opengps.Airport;
import net.dhleong.opengps.GpsRoute;
import net.dhleong.opengps.OpenGps;
import net.dhleong.opengps.R;
import net.dhleong.opengps.feat.routeinfo.RouteInfoView;
import net.dhleong.opengps.feat.waypoint.WaypointSearchView;
import net.dhleong.opengps.ui.DialogPrompter;
import net.dhleong.opengps.ui.NavigateUtil;
import net.dhleong.opengps.util.BasePresenter;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static net.dhleong.opengps.util.RxUtil.notNull;

/**
 * @author dhleong
 */
public class PreferredRoutesPresenter extends BasePresenter<PreferredRoutesView> {

    @Inject Context context;
    @Inject OpenGps gps;

    @Inject Action1<GpsRoute> routeUpdater;

    Airport origin, dest;

    @Inject PreferredRoutesPresenter() {}

    @Override
    public void onViewAttached(PreferredRoutesView view) {

        trySearch(view);

        subscribe(
            view.selectOriginRequests()
                .flatMap(this::pickAirport)
                .doOnNext(airport -> {
                    origin = airport;
                    trySearch(view);
                })
                .subscribe(view::setOrigin)
        );

        subscribe(
            view.selectDestRequests()
                .flatMap(this::pickAirport)
                .doOnNext(airport -> {
                    dest = airport;
                    trySearch(view);
                })
                .subscribe(view::setDestination)
        );

        subscribe(
            view.selectedRoutes()
                .flatMap(route ->
                    DialogPrompter.prompt(context, RouteInfoView.class, R.layout.feat_routeinfo, route))
                .filter(notNull())
                // NB: if we get here, they chose to load the route
                .flatMap(route -> route.gpsRoute(gps))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(gpsRoute -> {
                    Timber.v("loaded: %s", gpsRoute);

                    // save gpsRoute
                    routeUpdater.call(gpsRoute);

                    // also, go back to the Home (and into the FlightPlanner)
                    NavigateUtil.backFrom(view);
                    NavigateUtil.into(context, R.layout.feat_fpl);
                })
        );

    }

    void trySearch(PreferredRoutesView view) {
        final Airport from = origin;
        final Airport to = dest;
        if (from == null || to == null) {
            view.setLoading(false);
            view.setEmpty(false);
            return;
        }

        // reset state
        view.setEmpty(false);
        view.setLoading(true);

        Timber.v("Find routes between %s and %s", from, to);
        subscribe(
            gps.preferredRoutes(from, to)
               .subscribeOn(Schedulers.io())
               .toList()
               .observeOn(AndroidSchedulers.mainThread())
               .doOnNext(routes -> {
                   view.setLoading(false);
                   if (routes.isEmpty()) {
                       view.setEmpty(true);
                   }
               })
               .subscribe(view::setRoutes)
        );
    }

    Observable<Airport> pickAirport(Void ignore) {
        Timber.v("Pick airport");
        return DialogPrompter.prompt(context, WaypointSearchView.class, R.layout.feat_waypoint, Airport.class)
                             .cast(Airport.class);
    }
}
