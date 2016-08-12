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

        public TestableNasrDataSource(String apt, String ils) {
            super(new File(""));

            this.apt = apt;
            this.ils = ils;
        }

        @Override
        protected Source openAirportsFile() throws IOException {
            return source(apt);
        }

        @Override
        protected Source openIlsFile() throws IOException {
            return source(ils);
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
            NasrTextParsingTest.LGA_ILS_13_LOC
        );

        assertThat(dataSource.loadInto(storage).toBlocking().single()).isTrue();
        Airport airport = storage.airport("LGA").toBlocking().single();
        assertThat(airport)
            .hasId("LGA")
            .hasNavFrequencies(1);

        assertThat(airport.frequencies(Airport.FrequencyType.NAV))
            .usingFieldByFieldElementComparator()
            .containsExactly(new LabeledFrequency("ILS/DME 13", 108.5));

    }

}
