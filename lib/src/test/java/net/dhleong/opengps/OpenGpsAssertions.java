package net.dhleong.opengps;

import org.assertj.core.api.Assertions;

/**
 * @author dhleong
 */
public class OpenGpsAssertions extends Assertions {

    public static AirportAssert assertThat(Airport obj) {
        return new AirportAssert(obj);
    }

    public static AirwayAssert assertThat(Airway obj) {
        return new AirwayAssert(obj);
    }

    public static GpsRouteAssert assertThat(GpsRoute obj) {
        return new GpsRouteAssert(obj);
    }

    public static NavaidAssert assertThat(Navaid obj) {
        return new NavaidAssert(obj);
    }

    public static NavFixAssert assertThat(NavFix obj) {
        return new NavFixAssert(obj);
    }
}
