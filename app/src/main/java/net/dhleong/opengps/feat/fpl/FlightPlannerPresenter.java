package net.dhleong.opengps.feat.fpl;

import net.dhleong.opengps.AeroObject;
import net.dhleong.opengps.Airway;
import net.dhleong.opengps.GpsRoute;
import net.dhleong.opengps.OpenGps;
import net.dhleong.opengps.util.BasePresenter;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * @author dhleong
 */
public class FlightPlannerPresenter extends BasePresenter<FlightPlannerView> {

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
                .flatMap(airwayStart -> {
                    // TODO however we request the airway should
                    //  somehow be able to return the exit as well...
                    //  Perhaps a single Observable that emits a List
                    //  of AeroObjects?
                    Observable<Airway> airwayObs = gps.airway("V475").take(1);
                    Observable<AeroObject> exitObs = gps.find("PVD").take(1);
                    return Observable.zip(airwayObs, exitObs, (airway, exit) -> {
                        airway.appendPointsBetween(airwayStart, exit, route);
                        return null;
                    });
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
        );
    }
}
