package net.dhleong.opengps.feat.fpl;

import android.content.Context;

import net.dhleong.opengps.GpsRoute;
import net.dhleong.opengps.OpenGps;
import net.dhleong.opengps.R;
import net.dhleong.opengps.feat.airway.AirwaySearchView;
import net.dhleong.opengps.ui.DialogPrompter;
import net.dhleong.opengps.util.BasePresenter;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
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
                .observeOn(Schedulers.io())
                .flatMap(request -> gps.find("BDR").first()) // TODO real prompt
                .doOnNext(route::add)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(any -> {
                    Timber.v("Route = %s", route);
                    view.setRoute(route);
                })
        );

        subscribe(
            view.loadAirwayEvents()
                .observeOn(Schedulers.io())
                .flatMap(airwayStart ->
                    DialogPrompter.prompt(context, AirwaySearchView.class, R.layout.feat_airway, airwayStart))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    result.airway.appendPointsBetween(result.entry, result.exit, route);
                    view.setRoute(route);
                })
        );
    }
}
