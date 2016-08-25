package net.dhleong.opengps;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author dhleong
 */
public class NavFixAssert extends AeroObjectAssert<NavFixAssert, NavFix> {
    public NavFixAssert(NavFix actual) {
        super(actual, NavFixAssert.class);
    }

    public NavFixAssert hasRef(AeroObject refObj, float bearing) {
        isNotNull();

        assertThat(actual.refs)
            .describedAs("References")
            .contains(new NavFix.Reference(refObj, bearing));

        return myself;
    }
}
