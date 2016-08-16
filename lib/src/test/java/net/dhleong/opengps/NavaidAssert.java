package net.dhleong.opengps;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author dhleong
 */
public class NavaidAssert extends AeroObjectAssert<NavaidAssert, Navaid> {
    public NavaidAssert(Navaid actual) {
        super(actual, NavaidAssert.class);
    }

    public NavaidAssert hasType(Navaid.Type expectedType) {
        isNotNull();

        assertThat(actual.type())
            .describedAs("Navaid Type")
            .isEqualTo(expectedType);

        return myself;
    }

    public NavaidAssert hasFrequency(double expectedFreq) {
        isNotNull();

        assertThat(actual.freq())
            .describedAs("Frequency")
            .isEqualTo(expectedFreq);

        return myself;
    }
}
