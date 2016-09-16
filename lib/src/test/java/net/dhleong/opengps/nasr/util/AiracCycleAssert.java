package net.dhleong.opengps.nasr.util;

import org.assertj.core.api.AbstractAssert;

import java.util.Calendar;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author dhleong
 */
public class AiracCycleAssert extends AbstractAssert<AiracCycleAssert, AiracCycle> {
    public AiracCycleAssert(AiracCycle actual) {
        super(actual, AiracCycleAssert.class);
    }

    public AiracCycleAssert hasNumber(int expected) {
        isNotNull();

        assertThat(actual.number)
            .describedAs("number")
            .isEqualTo(expected);

        return myself;
    }

    public AiracCycleAssert starts(int year, int month, int day) {
        isNotNull();

        assertThat(actual.startDate)
            .describedAs("Airac start date")
            .isEqualToIgnoringHours(dateFrom(year, month, day));

        return myself;
    }

    public AiracCycleAssert ends(int year, int month, int day) {
        isNotNull();

        assertThat(actual.endDate)
            .describedAs("Airac end date")
            .isEqualToIgnoringHours(dateFrom(year, month, day));

        return myself;
    }

    public AiracCycleAssert nasrStarts(int year, int month, int day) {
        isNotNull();

        assertThat(actual.nasrStart)
            .describedAs("Nasr start date")
            .isEqualToIgnoringHours(dateFrom(year, month, day));

        return myself;
    }

    public AiracCycleAssert nasrEnds(int year, int month, int day) {
        isNotNull();

        assertThat(actual.nasrEnd)
            .describedAs("Nasr end date")
            .isEqualToIgnoringHours(dateFrom(year, month, day));

        return myself;
    }

    public AiracCycleAssert hasNasrUrl(String expected) {
        isNotNull();

        assertThat(actual.getNasrDataUrl())
            .describedAs("Nasr data url")
            .isEqualTo(expected);

        return myself;
    }

    static Date dateFrom(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        return cal.getTime();
    }
}
