package net.dhleong.opengps;

import net.dhleong.opengps.nasr.NasrTextDataSource;
import net.dhleong.opengps.storage.InMemoryStorage;

import org.junit.Test;

import java.io.File;

import static net.dhleong.opengps.OpenGpsAssertions.assertThat;

/**
 * @author dhleong
 */
public class OpenGpsTest {

    @Test
    public void test() {
        OpenGps gps = new OpenGps.Builder()
            .storage(new InMemoryStorage())
            .addDataSource(new NasrTextDataSource(new File("nasr-cache.zip")))
            .build();

        Airport laguardia = gps.airport("LGA").toBlocking().single();
        assertThat(laguardia)
            .isNotNull()
            .hasId("LGA")
            .hasNavFrequencies(5);
    }
}
