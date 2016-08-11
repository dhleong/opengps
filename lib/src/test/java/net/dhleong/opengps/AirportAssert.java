package net.dhleong.opengps;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.data.Percentage;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author dhleong
 */
public class AirportAssert extends AbstractAssert<AirportAssert, Airport> {
    public AirportAssert(Airport obj) {
        super(obj, AirportAssert.class);
    }

    public AirportAssert hasId(String expected) {
        isNotNull();

        assertThat(actual.id())
            .describedAs("airport ID")
            .isEqualTo(expected);
        return myself;
    }

    public AirportAssert isType(Airport.Type expected) {
        isNotNull();

        assertThat(actual.airportType())
            .describedAs("airport type")
            .isEqualTo(expected);
        return myself;
    }

    public AirportAssert hasName(String expected) {
        isNotNull();

        assertThat(actual.name)
            .describedAs("airport name")
            .isEqualTo(expected);
        return myself;
    }

    public AirportAssert hasNumber(String expected) {
        isNotNull();

        assertThat(actual.number())
            .describedAs("airport number")
            .isEqualTo(expected);
        return myself;
    }

    public AirportAssert hasLat(double expected) {
        isNotNull();

        assertThat(actual.lat())
            .describedAs("airport latitude")
            .isCloseTo(expected, Percentage.withPercentage(.001));
        return myself;
    }

    public AirportAssert hasLng(double expected) {
        isNotNull();

        assertThat(actual.lng())
            .describedAs("airport longitude")
            .isCloseTo(expected, Percentage.withPercentage(.001));
        return myself;
    }
}
