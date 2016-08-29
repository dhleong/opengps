package net.dhleong.opengps;

import net.dhleong.opengps.nasr.NasrTextDataSource;
import net.dhleong.opengps.storage.InMemoryStorage;
import net.dhleong.opengps.test.Airports;

import org.junit.Test;

import java.io.File;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;

import static net.dhleong.opengps.OpenGpsAssertions.assertThat;
import static net.dhleong.opengps.test.Navaids.BDR;
import static net.dhleong.opengps.test.Navaids.LGA;
import static net.dhleong.opengps.test.Navaids.MAD;
import static net.dhleong.opengps.test.Navaids.ORW;
import static net.dhleong.opengps.test.Navaids.PVD;

/**
 * @author dhleong
 */
public class OpenGpsTest {

    static OpenGps gps = build();

    @Test
    public void test() {
        Airport laguardia = gps.airport("LGA").toBlocking().single();
        assertThat(laguardia)
            .isNotNull()
            .hasId("LGA")
            .hasNavFrequencies(5);
    }

    @Test
    public void near() {
        List<AeroObject> objects =
            gps.anyNear(40.77, -73.86, 41)
               .toList()
               .toBlocking()
               .single();

        assertThat(filterBy(objects, obj -> "LGA".equals(obj.id())))
            .hasSize(2); // VOR and airport
    }

    @Test
    public void route_airwayOnly() {
        GpsRoute route =
            gps.parseRoute(Airports.LGA, Airports.PVD, Collections.singletonList("V475"), 0)
               .toBlocking()
               .single();

        assertThat(route)
            .containsFixesExactly(
                Airports.LGA,
                LGA, BDR, MAD, ORW, PVD,
                Airports.PVD).isNull();
    }

    static List<AeroObject> filterBy(List<AeroObject> obj, Func1<AeroObject, Boolean> filter) {
        // the lazy way, not the efficient way:
        return Observable.from(obj)
            .filter(filter)
            .toList()
            .toBlocking()
            .single();
    }

    public static OpenGps build() {
        return new OpenGps.Builder()
            .storage(new InMemoryStorage())
            .addDataSource(new NasrTextDataSource(new File("nasr-cache.zip")))
            .build();
    }

}
