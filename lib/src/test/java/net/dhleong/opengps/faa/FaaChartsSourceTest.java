package net.dhleong.opengps.faa;

import net.dhleong.opengps.ChartInfo;
import net.dhleong.opengps.status.StatusUpdate;
import net.dhleong.opengps.test.Airports;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import rx.Observer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author dhleong
 */
public class FaaChartsSourceTest {

    FaaChartsSource source;
    FaaChartsStorage storage;
    final Observer<StatusUpdate> updates = new Observer<StatusUpdate>() {
        @Override public void onCompleted() { }
        @Override public void onError(Throwable e) { }
        @Override public void onNext(StatusUpdate statusUpdate) { }
    };

    @Before
    public void setUp() {
        storage = new FaaChartsStorage();
        source = new FaaChartsSource(new File(".test-cache"));
        assertThat(source.loadInto(storage, updates).toBlocking().value()).isNotNull();
        // NB: if an error occured, it will have thrown from value()
    }

    @Test
    public void liveTest() {
        List<ChartInfo> charts = source.chartsFor(Airports.LGA).toBlocking().first();
        assertThat(charts).isNotEmpty();

        // the FAA intentionally sorts it differently, but it's convenient
        //  for this to be up-front
        assertThat(charts)
            .usingElementComparatorOnFields("name")
            .startsWith(new ChartInfo("AIRPORT DIAGRAM", null));
        assertThat(charts.get(0).name).isEqualTo("AIRPORT DIAGRAM");
    }

}