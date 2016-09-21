package net.dhleong.opengps.feat.charts;

import net.dhleong.opengps.AbstractStorage;
import net.dhleong.opengps.Airport;
import net.dhleong.opengps.ChartInfo;
import net.dhleong.opengps.DataSource;
import net.dhleong.opengps.Storage;

import java.util.List;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * @author dhleong
 */
public class AirChartsStorage extends AbstractStorage {

    AirChartsSource source;

    @Override
    public Observable<Storage> load() {
        return Observable.just(this);
    }

    @Override
    public void finishSource(DataSource source) {
        if (source instanceof AirChartsSource) {
            this.source = (AirChartsSource) source;
        }
    }

    @Override
    public boolean hasDataSource(DataSource source) {
        return false;
    }

    @Override
    public Observable<List<ChartInfo>> chartsFor(Airport airport) {
        return source.getCharts(airport.id())
                     .subscribeOn(Schedulers.io())
                     .map(result -> result.get(airport.id()).charts);
    }
}
