package net.dhleong.opengps.nasr;

import net.dhleong.opengps.Airport;
import net.dhleong.opengps.LabeledFrequency;
import net.dhleong.opengps.storage.InMemoryStorage;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import okio.Source;

import static net.dhleong.opengps.OpenGpsAssertions.assertThat;
import static net.dhleong.opengps.test.OkioTest.source;

/**
 * @author dhleong
 */
public class NasrTextDataSourceTest {

    static class TestableNasrDataSource extends NasrTextDataSource {
        private final String apt;
        private final String ils;
        private final String twr;

        public TestableNasrDataSource(String apt, String ils, String twr) {
            super(new File(""));

            this.apt = apt;
            this.ils = ils;
            this.twr = twr;
        }

        @Override
        protected Source openAirportsFile() throws IOException {
            return source(apt);
        }

        @Override
        protected Source openIlsFile() throws IOException {
            return source(ils);
        }

        @Override
        protected Source openTwrFile() throws IOException {
            return source(twr);
        }
    }

    InMemoryStorage storage;

    @Before
    public void setUp() {
        storage = new InMemoryStorage();
    }

    @Test
    public void read() {
        TestableNasrDataSource dataSource = new TestableNasrDataSource(
            NasrTextParsingTest.LAGUARDIA,
            NasrTextParsingTest.LGA_ILS_13_LOC,
            NasrTextParsingTest.SNA_TWR_COMBINED.replace(
                NasrTextParsingTest.SNA_NUMBER,
                NasrTextParsingTest.LGA_NUMBER
            )
        );

        assertThat(dataSource.loadInto(storage).toBlocking().single()).isTrue();
        Airport airport = storage.airport("LGA").toBlocking().single();
        assertThat(airport)
            .hasId("LGA")
            .hasNavFrequencies(1);

        assertThat(airport.frequencies(Airport.FrequencyType.NAV))
            .containsExactly(new LabeledFrequency("ILS/DME 13", 108.5));

        assertThat(airport.frequencies(Airport.FrequencyType.ATIS))
            .containsExactly(new LabeledFrequency("D-ATIS", 126.0));

        assertThat(airport.frequencies(Airport.FrequencyType.DELIVERY))
            .containsExactly(
                new LabeledFrequency("CD/P", 118.0),
                new LabeledFrequency("CD/S", 121.85)
            );

        assertThat(airport.frequencies(Airport.FrequencyType.GROUND))
            .containsExactly(
                new LabeledFrequency("GND/P EAST", 120.8),
                new LabeledFrequency("GND/P WEST", 132.25)
            );

        assertThat(airport.frequencies(Airport.FrequencyType.TOWER))
            .containsExactly(
                new LabeledFrequency("LCL/P (RWY 02R/20L)", 119.9),
                new LabeledFrequency("LCL/P IC (RWY 02L/20R)", 126.8),
                new LabeledFrequency("LCL/S", 128.35),
                new LabeledFrequency("LCL/P", 379.9)
            );
    }

}
