package net.dhleong.opengps.faa;

import net.dhleong.opengps.AbstractStorage;
import net.dhleong.opengps.Airport;
import net.dhleong.opengps.ChartInfo;
import net.dhleong.opengps.DataSource;

import java.util.List;

import rx.Observable;

/**
 * @author dhleong
 */
public class FaaChartsStorage extends AbstractStorage {
    private FaaChartsSource source;

    @Override
    public void finishSource(DataSource source) {
        if (source instanceof FaaChartsSource) {
            this.source = (FaaChartsSource) source;
        }
    }

    @Override
    public Observable<List<ChartInfo>> chartsFor(Airport airport) {
        return source.chartsFor(airport);
    }
}
