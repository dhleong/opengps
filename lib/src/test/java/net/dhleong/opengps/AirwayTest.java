package net.dhleong.opengps;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static net.dhleong.opengps.test.TestUtil.dmsToDegrees;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author dhleong
 */
public class AirwayTest {

    static final AeroObject LGA = new Navaid(Navaid.Type.VORDME, "LGA", "LA GUARDIA",
        dmsToDegrees(40, 47, 1.376),
        dmsToDegrees(-73, -52, -6.962),
        0); // we don't care about frequencies...
    static final AeroObject BDR = new Navaid(Navaid.Type.VORDME, "BDR", "BRIDGEPORT",
        dmsToDegrees(41, 9, 38.495),
        dmsToDegrees(-73, -7, -28.188),
        0);
    static final AeroObject MAD = new Navaid(Navaid.Type.VORDME, "MAD", "MADISON",
        dmsToDegrees(41, 18, 49.811),
        dmsToDegrees(-72, -41, -31.893),
        0);
    static final AeroObject ORW = new Navaid(Navaid.Type.VORDME, "ORW", "NORWICH",
        dmsToDegrees(41, 33, 23.053),
        dmsToDegrees(-71, -59, -57.672),
        0);
    static final AeroObject PVD = new Navaid(Navaid.Type.VORTAC, "PVD", "PROVIDENCE",
        dmsToDegrees(41, 43, 27.639),
        dmsToDegrees(-71, -25, -46.699),
        0);

    Airway airway = new Airway("V475", Arrays.asList(
        // TODO fixes
        LGA,
        BDR,
        MAD,
        ORW,
        PVD));

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
        Navaid hfd = new Navaid(Navaid.Type.VORDME, "HFD", "HARTFORD",
            dmsToDegrees(41, 38, 27.997), dmsToDegrees(-72, -32, -50.705), 0);
        assertThat(airway.nearestTo(hfd))
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