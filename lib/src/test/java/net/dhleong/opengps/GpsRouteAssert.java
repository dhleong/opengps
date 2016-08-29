package net.dhleong.opengps;

import org.assertj.core.api.AbstractAssert;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author dhleong
 */
public class GpsRouteAssert extends AbstractAssert<GpsRouteAssert, GpsRoute> {
    public GpsRouteAssert(GpsRoute actual) {
        super(actual, GpsRouteAssert.class);
    }

    public GpsRouteAssert containsFixesExactly(AeroObject... expectedFixes) {
        isNotNull();

        final List<AeroObject> actualFixes = new ArrayList<>();
        for (GpsRoute.Step s : actual.steps()) {
            if (s.type == GpsRoute.Step.Type.FIX) {
                actualFixes.add(s.ref);
            }
        }

        assertThat(actualFixes)
            .describedAs("GPS Fixes")
            .containsExactly(expectedFixes);

        return myself;
    }
}
