package net.dhleong.opengps;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author dhleong
 */
public class AirwayAssert extends AeroObjectAssert<AirwayAssert, Airway> {
    public AirwayAssert(Airway actual) {
        super(actual, AirwayAssert.class);
    }

    public AirwayAssert containsExactly(AeroObject... objects) {
        isNotNull();

        assertThat(actual.points)
            .describedAs("Airway points")
            .containsExactly(objects);

        return myself;
    }
}
