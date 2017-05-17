package net.dhleong.opengps.nasr.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Information about, and factory to calculate,
 *  the current Airac cycle
 * @author dhleong
 */
public class AiracCycle {
    static final Calendar AIRAC1601 = Calendar.getInstance();
    static final Calendar NASR2016 = Calendar.getInstance();
    static final Calendar NASR2017 = Calendar.getInstance();

    static {
        AIRAC1601.set(2016, Calendar.JANUARY, 7);
        NASR2016.set(2016, Calendar.FEBRUARY, 4);
        NASR2017.set(2017, Calendar.MARCH, 2);
    }

    static final long DAY_IN_MILLIS = 24 * 3600 * 1000;

    static final int AIRAC_PERIOD_DAYS = 28;
    static final int NASR_2016_PERIOD_DAYS = 56;
    static final int NASR_2017_PERIOD_DAYS = 28;

    static final String NASR_2016_URL_FORMAT = "https://nfdc.faa.gov/webContent/56DaySub/56DySubscription_%s_-_%s.zip";
    static final String NASR_2016_DATE_FORMAT = "MMMM_dd__yyyy";
    static final String NASR_2017_URL_FORMAT = "https://nfdc.faa.gov/webContent/28DaySub/28DaySubscription_Effective_%s.zip";
    static final String NASR_2017_DATE_FORMAT = "yyyy-MM-dd";

    static final String FAA_CHARTS_URL_FORMAT = "https://nfdc.faa.gov/webContent/dtpp/%d.xml";

    public final int number;
    public final Date startDate;
    public final Date endDate;
    public final Date nasrStart;
    public final Date nasrEnd;

    private AiracCycle(int number, Date startDate, Date endDate, Date nasrStart, Date nasrEnd) {
        this.number = number;
        this.startDate = startDate;
        this.endDate = endDate;
        this.nasrStart = nasrStart;
        this.nasrEnd = nasrEnd;
    }

    public String getFaaChartUrl() {
        return String.format(Locale.US, FAA_CHARTS_URL_FORMAT, number);
    }

    public String getNasrDataUrl() {
        if (nasrStart.getTime() < NASR2017.getTimeInMillis()) {
            SimpleDateFormat format = new SimpleDateFormat(NASR_2016_DATE_FORMAT, Locale.US);
            String start = format.format(nasrStart);
            String end = format.format(nasrEnd);
            return String.format(NASR_2016_URL_FORMAT, start, end);
        } else {
            SimpleDateFormat format = new SimpleDateFormat(NASR_2017_DATE_FORMAT, Locale.US);
            String start = format.format(nasrStart);
            return String.format(NASR_2017_URL_FORMAT, start);
        }
    }

    public static AiracCycle current() {
        Calendar now = Calendar.getInstance();
        return forCalendar(now);
    }

    public static AiracCycle forCalendar(final Calendar calendar) {
        final long airac1601Millis = AIRAC1601.getTimeInMillis();
        final long now = calendar.getTimeInMillis();
        final long deltaMillis = now - airac1601Millis;
        final long days = deltaMillis / DAY_IN_MILLIS;
        final int airacPeriods = (int) (days / AIRAC_PERIOD_DAYS);
        final int airacYears = airacPeriods / 13;
        final int airacPeriod = airacPeriods % 13;
        int year = AIRAC1601.get(Calendar.YEAR);
        int airacNumber = ((year + airacYears) % 100) * 100 + airacPeriod + 1;

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(airac1601Millis);
        cal.add(Calendar.DAY_OF_YEAR, airacPeriods * AIRAC_PERIOD_DAYS);
        final Date airacStart = cal.getTime();
        cal.add(Calendar.DAY_OF_YEAR, AIRAC_PERIOD_DAYS);
        final Date airacEnd = cal.getTime();

        // NB: The NASR subscription is not tied directly to an airac cycle
        final long nasrFirst;
        final int nasrPeriodDays;
        if (now < NASR2017.getTimeInMillis()) {
            nasrFirst = NASR2016.getTimeInMillis();
            nasrPeriodDays = NASR_2016_PERIOD_DAYS;
        } else {
            nasrFirst = NASR2017.getTimeInMillis();
            nasrPeriodDays = NASR_2017_PERIOD_DAYS;
        }

        final long nasrDelta = now - nasrFirst;
        final long nasrDays = nasrDelta / DAY_IN_MILLIS;
        final int nasrPeriods = (int) (nasrDays / nasrPeriodDays);
        cal.setTimeInMillis(nasrFirst);
        cal.add(Calendar.DAY_OF_YEAR, nasrPeriods * nasrPeriodDays);
        final Date nasrStart = cal.getTime();
        cal.add(Calendar.DAY_OF_YEAR, nasrPeriodDays);
        final Date nasrEnd = cal.getTime();

        return new AiracCycle(airacNumber, airacStart, airacEnd, nasrStart, nasrEnd);
    }

}
