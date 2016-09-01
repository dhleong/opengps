package net.dhleong.opengps;

import net.dhleong.opengps.test.Airports;
import net.dhleong.opengps.test.Navaids;

import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static net.dhleong.opengps.OpenGpsAssertions.assertThat;
import static net.dhleong.opengps.test.Navaids.BDR;
import static net.dhleong.opengps.test.Navaids.HFD;
import static net.dhleong.opengps.test.Navaids.LGA;
import static net.dhleong.opengps.test.Navaids.MAD;
import static net.dhleong.opengps.test.Navaids.ORW;
import static net.dhleong.opengps.test.Navaids.PVD;
import static net.dhleong.opengps.test.TestUtil.dmsToDegrees;

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
        appendAirwayPoints(LGA, PVD, list);

        assertThat(list).containsExactly(LGA, BDR, MAD, ORW, PVD);
    }

    @Test
    public void appendBetween_fullBackward() {
        ArrayList<AeroObject> list = new ArrayList<>();
        appendAirwayPoints(PVD, LGA, list);

        assertThat(list).containsExactly(PVD, ORW, MAD, BDR, LGA);
    }

    @Test
    public void appendBetween_partialForward() {
        ArrayList<AeroObject> list = new ArrayList<>();
        appendAirwayPoints(MAD, ORW, list);

        assertThat(list).containsExactly(MAD, ORW);
    }

    @Test
    public void appendBetween_partialBackward() {
        ArrayList<AeroObject> list = new ArrayList<>();
        appendAirwayPoints(ORW, MAD, list);

        assertThat(list).containsExactly(ORW, MAD);
    }

    @Test
    public void appendBetween_existingEntry() {
        GpsRoute route = new GpsRoute(0);
        route.add(BDR);
        airway.appendPointsBetween(BDR, ORW, route);

        assertThat(route)
            .hasSize(7)
            .containsFixesExactly(BDR, MAD, ORW)
            .containsExactly(
                GpsRoute.Step.fix(BDR),
                GpsRoute.Step.to(MAD, 77, 22),
                GpsRoute.Step.airway(airway),
                GpsRoute.Step.fix(MAD),
                GpsRoute.Step.to(ORW, 78, 34),
                GpsRoute.Step.fix(ORW),
                GpsRoute.Step.airwayExit(airway)
            )
        ;
    }

    @Test
    public void appendBetween_existingEntry_indexed() {
        GpsRoute route = new GpsRoute(0);
        route.add(Airports.LGA);
        route.add(BDR);

        int bdrIndex = route.indexOfWaypoint(BDR);
        airway.appendPointsBetween(BDR, ORW, route, bdrIndex + 1);

        assertThat(route)
            .hasSize(9)
            .containsFixesExactly(Airports.LGA, BDR, MAD, ORW)
            .containsExactly(
                GpsRoute.Step.fix(Airports.LGA),
                GpsRoute.Step.to(BDR, 68, 41),
                GpsRoute.Step.fix(BDR),
                GpsRoute.Step.to(MAD, 77, 22),
                GpsRoute.Step.airway(airway),
                GpsRoute.Step.fix(MAD),
                GpsRoute.Step.to(ORW, 78, 34),
                GpsRoute.Step.fix(ORW),
                GpsRoute.Step.airwayExit(airway)
            )
        ;
    }

    @Test
    public void appendBetween_nop() {
        GpsRoute route = new GpsRoute(0);
        route.add(Navaids.BDR);
        route.add(Navaids.MAD);
        airway.appendPointsBetween(BDR, MAD, route, 0);

        assertThat(route).containsFixesExactly(BDR, MAD);
    }

    @Test
    @Ignore("TODO test appending to an index in reverse order")
    public void appendBetween_indexed_reverse() {
        // NB make sure it doesn't crash
    }

    @Test
    public void appendAfterAppend() {

        Airway airway2 = new Airway("V1", Arrays.asList(
            // NB: should also be fixes, but... shouldn't matter
            MAD, ORW, PVD
        ));

        GpsRoute route = new GpsRoute(0);
        route.add(LGA);
        airway.appendPointsBetween(LGA, MAD, route, 0);
        airway2.appendPointsBetween(MAD, PVD, route, route.indexOfWaypoint(MAD) + 1);

        assertThat(route)
            .containsExactly(
                GpsRoute.Step.fix(LGA),
                GpsRoute.Step.to(BDR, 68, 41),
                GpsRoute.Step.airway(airway),
                GpsRoute.Step.fix(BDR),
                GpsRoute.Step.to(MAD, 77, 22),
                GpsRoute.Step.fix(MAD),
                GpsRoute.Step.airwayExit(airway),
                GpsRoute.Step.to(ORW, 78, 34),
                GpsRoute.Step.airway(airway2),
                GpsRoute.Step.fix(ORW),
                GpsRoute.Step.to(PVD, 82, 27),
                GpsRoute.Step.fix(PVD),
                GpsRoute.Step.airwayExit(airway2)
            );
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

    private void appendAirwayPoints(AeroObject from, AeroObject to, ArrayList<AeroObject> list) {
        GpsRoute route = new GpsRoute();
        airway.appendPointsBetween(from, to, route);
        for (GpsRoute.Step s : route.steps()) {
            if (s.type == GpsRoute.Step.Type.FIX) {
                list.add(s.ref);
            }
        }
    }

}