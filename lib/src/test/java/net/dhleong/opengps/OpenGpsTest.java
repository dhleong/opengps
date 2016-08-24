package net.dhleong.opengps;

import net.dhleong.opengps.nasr.NasrTextDataSource;
import net.dhleong.opengps.storage.InMemoryStorage;

import org.junit.Test;

import java.io.File;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;

import static net.dhleong.opengps.OpenGpsAssertions.assertThat;

/**
 * @author dhleong
 */
public class OpenGpsTest {

    OpenGps gps = new OpenGps.Builder()
        .storage(new InMemoryStorage())
        .addDataSource(new NasrTextDataSource(new File("nasr-cache.zip")))
        .build();

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
            gps.anyNear(40.77, -73.86, 10)
               .toList()
               .toBlocking()
               .single();

        assertThat(filterBy(objects, obj -> "LGA".equals(obj.id())))
            .hasSize(2); // VOR and airport
    }

    static List<AeroObject> filterBy(List<AeroObject> obj, Func1<AeroObject, Boolean> filter) {
        // the lazy way, not the efficient way:
        return Observable.from(obj)
            .filter(filter)
            .toList()
            .toBlocking()
            .single();
    }
}
