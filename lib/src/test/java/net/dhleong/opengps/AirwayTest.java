package net.dhleong.opengps;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static net.dhleong.opengps.test.Navaids.BDR;
import static net.dhleong.opengps.test.Navaids.HFD;
import static net.dhleong.opengps.test.Navaids.LGA;
import static net.dhleong.opengps.test.Navaids.MAD;
import static net.dhleong.opengps.test.Navaids.ORW;
import static net.dhleong.opengps.test.Navaids.PVD;
import static net.dhleong.opengps.test.TestUtil.dmsToDegrees;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author dhleong
 */
public class AirwayTest {

    Airway airway = new Airway("V475", Arrays.asList(
        // NB: should also be fixes, but... shouldn't matter
        LGA, BDR, MAD, ORW, PVD
    ));

    @Test
    public void appendBetween_fullForward() {
        ArrayList<AeroObject> list = new ArrayList<>();
        airway.appendPointsBetween(LGA, PVD, list);

        assertThat(list).containsExactly(LGA, BDR, MAD, ORW, PVD);
    }

    @Test
    public void appendBetween_fullBackward() {
        ArrayList<AeroObject> list = new ArrayList<>();
        airway.appendPointsBetween(PVD, LGA, list);

        assertThat(list).containsExactly(PVD, ORW, MAD, BDR, LGA);
    }

    @Test
    public void appendBetween_partialForward() {
        ArrayList<AeroObject> list = new ArrayList<>();
        airway.appendPointsBetween(MAD, ORW, list);

        assertThat(list).containsExactly(MAD, ORW);
    }

    @Test
    public void appendBetween_partialBackward() {
        ArrayList<AeroObject> list = new ArrayList<>();
        airway.appendPointsBetween(ORW, MAD, list);

        assertThat(list).containsExactly(ORW, MAD);
    }

    @Test
    public void nearest_start() {
        Airport klga = new Airport("", Airport.Type.AIRPORT, "KLGA", "LAGUARDIA",
            dmsToDegrees(40, 46, 38.1), dmsToDegrees(-73, -52, -21.4));
        assertThat(airway.nearestTo(klga))
            .isEqualTo(LGA);
    }

    @Test
    public void nearest_middle() {
        assertThat(airway.nearestTo(HFD))
            .isEqualTo(MAD);
    }

    @Test
    public void nearest_end() {
        Airport kbos = new Airport("", Airport.Type.AIRPORT, "BOS", "GENERAL EDWARD LAWRENCE LOGAN INTL",
            dmsToDegrees(42, 21, 46.6), dmsToDegrees(-71, 0, -23));
        assertThat(airway.nearestTo(kbos))
            .isEqualTo(PVD);
    }
}