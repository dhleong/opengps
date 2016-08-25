package net.dhleong.opengps;

import net.dhleong.NavFix;

import org.assertj.core.api.Assertions;

/**
 * @author dhleong
 */
public class OpenGpsAssertions extends Assertions {

    public static AirportAssert assertThat(Airport obj) {
        return new AirportAssert(obj);
    }

    public static NavaidAssert assertThat(Navaid obj) {
        return new NavaidAssert(obj);
    }

    public static NavFixAssert assertThat(NavFix obj) {
        return new NavFixAssert(obj);
    }
}
