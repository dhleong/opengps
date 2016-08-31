package net.dhleong.opengps;

import net.dhleong.opengps.test.Airports;
import net.dhleong.opengps.test.Navaids;

import org.junit.Test;

import static net.dhleong.opengps.OpenGpsAssertions.assertThat;
import static net.dhleong.opengps.test.Navaids.BDR;
import static net.dhleong.opengps.test.Navaids.MAD;
import static net.dhleong.opengps.test.Navaids.ORW;

/**
 * @author dhleong
 */
public class GpsRouteTest {

    @Test
    public void append() {
        GpsRoute route = new GpsRoute();
        route.add(BDR);
        route.add(MAD);
        route.add(ORW);

        assertThat(route)
            .containsFixesExactly(BDR, MAD, ORW);
    }

    @Test
    public void remove_only() {
        GpsRoute route = new GpsRoute();
        route.add(BDR);
        assertThat(route).hasSize(1);

        route.removeStep(0);
        assertThat(route).hasSize(0);
    }

    @Test
    public void remove_first() {
        GpsRoute route = new GpsRoute();
        route.add(BDR);
        route.add(MAD);
        assertThat(route).hasSize(3);

        route.removeStep(0);
        assertThat(route)
            .hasSize(1)
            .containsFixesExactly(MAD);
    }

    @Test
    public void remove_last() {
        GpsRoute route = new GpsRoute();
        route.add(BDR);
        route.add(MAD);
        assertThat(route).hasSize(3);

        route.removeStep(2);
        assertThat(route)
            .hasSize(1)
            .containsFixesExactly(BDR);
    }

    @Test
    public void remove_first_nearby() {
        // NB: no `from`, only `to`
        GpsRoute route = new GpsRoute();
        route.add(Airports.LGA);
        route.add(Navaids.LGA);
        assertThat(route).hasSize(3);

        route.removeStep(0);
        assertThat(route)
            .hasSize(1)
            .containsFixesExactly(Navaids.LGA);
    }

    @Test
    public void remove_last_nearby() {
        // NB: no `from`, only `to`
        GpsRoute route = new GpsRoute();
        route.add(Airports.LGA);
        route.add(Navaids.LGA);
        assertThat(route).hasSize(3);

        route.removeStep(2);
        assertThat(route)
            .hasSize(1)
            .containsFixesExactly(Airports.LGA);
    }


    @Test
    public void remove_middle() {
        GpsRoute route = new GpsRoute();
        route.add(BDR);
        route.add(MAD);
        route.add(ORW);
        assertThat(route).hasSize(5);

        route.removeStep(2);
        assertThat(route)
            .hasSize(3)
            .containsFixesExactly(BDR, ORW)
            .containsExactly(
                GpsRoute.Step.fix(BDR),
                GpsRoute.Step.to(ORW, 77f, 56f),
                GpsRoute.Step.fix(ORW)
            );
    }

    @Test
    public void remove_middle_bothNearby() {
        GpsRoute route = new GpsRoute();
        route.add(Airports.LGA);
        route.add(Navaids.LGA);
        route.add(Airports.LGA);
        assertThat(route).hasSize(5);

        route.removeStep(2);
        assertThat(route)
            .containsExactly(
                GpsRoute.Step.fix(Airports.LGA),
                GpsRoute.Step.to(Airports.LGA, 0, 0),
                GpsRoute.Step.fix(Airports.LGA)
            );
    }
}
