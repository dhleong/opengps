package net.dhleong.opengps;

import org.assertj.core.api.Assertions;

/**
 * @author dhleong
 */
public class OpenGpsAssertions extends Assertions {

    public static AirportAssert assertThat(Airport obj) {
        return new AirportAssert(obj);
    }
}
