package net.dhleong.opengps.storage;

import net.dhleong.opengps.AeroObject;
import net.dhleong.opengps.Airport;
import net.dhleong.opengps.Navaid;
import net.dhleong.opengps.Storage;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static net.dhleong.opengps.test.TestUtil.dmsToDegrees;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author dhleong
 */
public class InMemoryStorageTest {

    Storage storage;

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
    }

    @Test
    public void queryNear() {
        List<AeroObject> objs =
            storage.findNear(40.77, -73.86, 1)
                   .toList()
                   .toBlocking()
                   .single();

        assertThat(objs).hasSize(2);
        assertThat(objs.get(0)).isInstanceOf(Airport.class);
        assertThat(objs.get(1)).isInstanceOf(Navaid.class);
    }
}
