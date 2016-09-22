package net.dhleong.opengps.feat.routes;

import android.content.Context;

import net.dhleong.opengps.Airport;
import net.dhleong.opengps.OpenGps;
import net.dhleong.opengps.R;
import net.dhleong.opengps.feat.waypoint.WaypointSearchView;
import net.dhleong.opengps.ui.DialogPrompter;
import net.dhleong.opengps.util.BasePresenter;

import javax.inject.Inject;

import rx.Observable;
import timber.log.Timber;

/**
 * @author dhleong
 */
public class PreferredRoutesPresenter extends BasePresenter<PreferredRoutesView> {

    @Inject Context context;
    @Inject OpenGps gps;

    Airport origin, dest;

    @Inject PreferredRoutesPresenter() {}

    @Override
    public void onViewAttached(PreferredRoutesView view) {

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
                .subscribe(route -> {
                    // TODO show more information; prompt to load into GPS
                    Timber.v("Selected %s", route);
                })
        );

    }

    void trySearch(PreferredRoutesView view) {
        if (origin == null || dest == null) return;

        // TODO
        Timber.v("Find routes between %s and %s", origin, dest);
    }

    Observable<Airport> pickAirport(Void ignore) {
        Timber.v("Pick airport");
        return DialogPrompter.prompt(context, WaypointSearchView.class, R.layout.feat_waypoint, Airport.class)
                             .cast(Airport.class);
    }
}
