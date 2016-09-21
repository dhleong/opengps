package net.dhleong.opengps.feat.charts;

import net.dhleong.opengps.DataSource;
import net.dhleong.opengps.Storage;

import javax.inject.Inject;

import rx.Observable;

/**
 * @author dhleong
 */
public class AirChartsSource implements DataSource {

    @Inject ChartsService service;

    @Inject AirChartsSource() {}

    @Override
    public String id() {
        return "aircharts";
    }

    @Override
    public Observable<Boolean> loadInto(Storage storage) {
        storage.finishSource(this);
        return Observable.just(true);
    }

    Observable<AirportCharts> getCharts(String icao) {
        return service.getCharts(icao);
    }

}
