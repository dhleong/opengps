package net.dhleong.opengps.nasr.util;

import org.junit.Test;

import java.util.Calendar;

import static net.dhleong.opengps.OpenGpsAssertions.assertThat;

/**
 * @author dhleong
 */
public class AiracCycleTest {
    @Test
    public void for1609() {
        // 1609 was Aug 18 to Sept 15
        Calendar calendar = Calendar.getInstance();
        calendar.set(2016, Calendar.AUGUST, 20);
        assertThat(AiracCycle.forCalendar(calendar))
            .hasNumber(1609)
            .starts(2016, Calendar.AUGUST, 18)
            .ends(2016, Calendar.SEPTEMBER, 15)
            // the nasr period was july 21 to sep 15
            .nasrStarts(2016, Calendar.JULY, 21)
            .nasrEnds(2016, Calendar.SEPTEMBER, 15)
            .hasNasrUrl("https://nfdc.faa.gov/webContent/56DaySub/56DySubscription_July_21__2016_-_September_15__2016.zip");
    }

    @Test
    public void for1610() {
        // 1610 is Sept 15 to Oct 14
        Calendar calendar = Calendar.getInstance();
        calendar.set(2016, Calendar.SEPTEMBER, 16);
        assertThat(AiracCycle.forCalendar(calendar))
            .hasNumber(1610)
            .starts(2016, Calendar.SEPTEMBER, 15)
            .ends(2016, Calendar.OCTOBER, 13) // NB: it's technically the 14th, but the math doesn't add up otherwise...
            // the nasr period was sep 15 to nov 10
            .nasrStarts(2016, Calendar.SEPTEMBER, 15)
            .nasrEnds(2016, Calendar.NOVEMBER, 10)
            .hasNasrUrl("https://nfdc.faa.gov/webContent/56DaySub/56DySubscription_September_15__2016_-_November_10__2016.zip");
    }

    @Test
    public void for1611() {
        // 1611 is Oct 14 to Nov 10
        Calendar calendar = Calendar.getInstance();
        calendar.set(2016, Calendar.OCTOBER, 15);
        assertThat(AiracCycle.forCalendar(calendar))
            .hasNumber(1611)
            .starts(2016, Calendar.OCTOBER, 13) // see above
            .ends(2016, Calendar.NOVEMBER, 10)
            // the nasr period is the same as 1610
            .nasrStarts(2016, Calendar.SEPTEMBER, 15)
            .nasrEnds(2016, Calendar.NOVEMBER, 10)
            .hasNasrUrl("https://nfdc.faa.gov/webContent/56DaySub/56DySubscription_September_15__2016_-_November_10__2016.zip");
    }

    @Test
    public void for1701() {
        // 1701 will be Jan 5 to Feb 4
        Calendar calendar = Calendar.getInstance();
        calendar.set(2017, Calendar.JANUARY, 20);
        assertThat(AiracCycle.forCalendar(calendar))
            .hasNumber(1701)
            .starts(2017, Calendar.JANUARY, 5)
            .ends(2017, Calendar.FEBRUARY, 2);
    }
}