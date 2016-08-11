package net.dhleong.opengps;

/**
 * @author dhleong
 */
public class OpenGpsAssertions {

    public static AirportAssert assertThat(Airport obj) {
        return new AirportAssert(obj);
    }
}
