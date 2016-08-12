package net.dhleong.opengps.nasr;

import java.io.File;
import java.io.IOException;

import okio.Source;

import static net.dhleong.opengps.test.OkioTest.source;

/**
 * @author dhleong
 */
public class NasrTextDataSourceTest {

    static class TestableNasrDataSource extends NasrTextDataSource {
        private final String apt;

        public TestableNasrDataSource(String apt) {
            super(new File(""));

            this.apt = apt;
        }

        @Override
        protected Source openAirportsFile() throws IOException {
            return source(apt);
        }

    }

}
