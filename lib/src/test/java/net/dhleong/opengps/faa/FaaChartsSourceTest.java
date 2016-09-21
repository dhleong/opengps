package net.dhleong.opengps.faa;

import net.dhleong.opengps.ChartInfo;
import net.dhleong.opengps.test.Airports;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author dhleong
 */
public class FaaChartsSourceTest {

    FaaChartsSource source;
    FaaChartsStorage storage;

    @Before
    public void setUp() {
        storage = new FaaChartsStorage();
        source = new FaaChartsSource(new File(".test-cache"));
        assertThat(source.loadInto(storage).toBlocking().first()).isTrue();
    }

    @Test
    public void liveTest() {
        List<ChartInfo> charts = source.chartsFor(Airports.LGA).toBlocking().first();
        assertThat(charts).isNotEmpty();

        // NB: we might change sorting in the future so this is the case,
        //  but the FAA intentionally sorts it differently
//        assertThat(charts.get(0).name).isEqualTo("AIRPORT DIAGRAM");
    }

}