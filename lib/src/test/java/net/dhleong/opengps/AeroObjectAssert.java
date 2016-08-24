package net.dhleong.opengps;

import org.assertj.core.api.AbstractAssert;

import static net.dhleong.opengps.test.TestUtil.dmsToDegrees;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author dhleong
 */
public abstract class AeroObjectAssert<S extends AbstractAssert<S, A>, A extends AeroObject>
        extends AbstractAssert<S, A> {

    public AeroObjectAssert(A actual, Class<S> selfType) {
        super(actual, selfType);
    }

    public S hasId(String expected) {
        isNotNull();

        assertThat(actual.id())
            .describedAs("AeroObject ID")
            .isEqualTo(expected);
        return myself;
    }


    public S hasName(String expected) {
        isNotNull();

        assertThat(actual.name())
            .describedAs("AeroObject name")
            .isEqualTo(expected);
        return myself;
    }

    public S hasLat(double expected) {
        isNotNull();

        assertThat(actual.lat())
            .describedAs("AeroObject latitude")
            .isStrictlyBetween(expected - 0.001, expected + 0.001);
        return myself;
    }

    public S hasLat(double expectedDegrees, double expectedMinutes, double expectedSeconds) {
        hasLat(dmsToDegrees(expectedDegrees, expectedMinutes, expectedSeconds));
        return myself;
    }

    public S hasLng(double expected) {
        isNotNull();

        assertThat(actual.lng())
            .describedAs("AeroObject longitude")
            .isStrictlyBetween(expected - 0.001, expected + 0.001);
        return myself;
    }

    public S hasLng(double expectedDegrees, double expectedMinutes, double expectedSeconds) {
        hasLng(dmsToDegrees(expectedDegrees, expectedMinutes, expectedSeconds));
        return myself;
    }


}
