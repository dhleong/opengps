package net.dhleong.opengps;

import org.assertj.core.data.Percentage;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author dhleong
 */
public class AirportAssert extends AeroObjectAssert<AirportAssert, Airport> {
    public AirportAssert(Airport obj) {
        super(obj, AirportAssert.class);
    }

    public AirportAssert isType(Airport.Type expected) {
        isNotNull();

        assertThat(actual.airportType())
            .describedAs("airport type")
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

    public AirportAssert hasElevation(float expected) {
        isNotNull();

        assertThat(actual.elevation)
            .describedAs("airport elevation")
            .isCloseTo(expected, Percentage.withPercentage(.001));
        return myself;
    }

    public AirportAssert hasNavFrequencies(int expectedCount) {
        isNotNull();

        assertThat(actual.frequencies(Airport.FrequencyType.NAV))
            .describedAs("ILS frequencies")
            .hasSize(expectedCount);
        return myself;
    }
}
