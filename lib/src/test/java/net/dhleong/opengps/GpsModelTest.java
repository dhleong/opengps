package net.dhleong.opengps;

import net.dhleong.opengps.storage.InMemoryStorage;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import rx.Observable;

import static net.dhleong.opengps.test.TestUtil.dmsToDegrees;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author dhleong
 */
public class GpsModelTest {
    final DataSource dummySource = new DataSource() {
        @Override
        public String id() {
            return "dummy";
        }

        @Override
        public Observable<Boolean> loadInto(Storage storage) {
            return Observable.just(true);
        }
    };

    Storage storage;
    OpenGps gps;
    GpsModel model;

    @Before
    public void setUp() {
        storage = new InMemoryStorage();
        storage.put(new Airport("lga", Airport.Type.AIRPORT,
            "LGA", "LAGUARDIA",
            dmsToDegrees(40, 46, 38.1), dmsToDegrees(-73, -52, -21.4)));
        storage.put(new Navaid(Navaid.Type.NDBDME,
            "LGA", "LAGUARDIA",
            dmsToDegrees(40, 47, 1.376), dmsToDegrees(-73, -52, -6.962),
            113.1));
        storage.finishSource(dummySource);

        gps = new OpenGps.Builder()
            .storage(storage)
            .addDataSource(dummySource)
            .build();

        model = new GpsModel(gps);
    }

    @Test
    public void simpleEnterExit() {
        ArrayList<AeroObject> discovered = new ArrayList<>();
        ArrayList<AeroObject> removed = new ArrayList<>();

        model.discoveredObjects().subscribe(discovered::add);
        model.removedObjects().subscribe(removed::add);

        assertThat(discovered).isEmpty();
        assertThat(removed).isEmpty();

        model.setLocation(dmsToDegrees(40, 47, 1.376), dmsToDegrees(-73, -52, -6.962), 10);

        model.discoveredObjects().skip(1).take(1).toBlocking().single();
        assertThat(discovered).hasSize(2);
        assertThat(removed).isEmpty();
    }
}
