package net.dhleong.opengps.util.wx;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author dhleong
 */
@Root(strict = false)
public class NoaaWxResponse {
    @Element(name = "raw_text")
    @Path("data/METAR")
    public String metar;

    @Element(name = "temp_c")
    @Path("data/METAR")
    public float temp;

    @Element(name = "dewpoint_c")
    @Path("data/METAR")
    public float dewpoint;

    @Element(name = "wind_dir_degrees")
    @Path("data/METAR")
    public int windDir;

    @Element(name = "wind_speed_kt")
    @Path("data/METAR")
    public int windSpeed;

    @Element(name = "visibility_statute_mi")
    @Path("data/METAR")
    public float visibility;

    @Element(name = "altim_in_hg")
    @Path("data/METAR")
    public float altimeter;

    @ElementList(entry = "sky_condition", inline = true)
    @Path("data/METAR")
    public List<SkyCondition> conditions;

    private Date observationDate;

    public static class SkyCondition {
        @Attribute(name = "sky_cover")
        public String type;

        @Attribute(name = "cloud_base_ft_agl", required = false)
        public int cloudBase;

        @Override
        public String toString() {
            return "SkyCondition{" +
                "type='" + type + '\'' +
                ", cloudBase=" + cloudBase +
                '}';
        }
    }

    @SuppressWarnings("deprecation")
    public Date getObservationDate() {
        Date cached = observationDate;
        if (cached != null) return cached;

        String rawDate = metar.substring(5, 11);
        try {
            final Calendar today = Calendar.getInstance();
            final Date parsed = new SimpleDateFormat("ddHHmm", Locale.US).parse(rawDate);

            final int theDay = parsed.getDay();
            if (theDay > today.get(Calendar.DAY_OF_MONTH)) {
                final int actualMonth = today.get(Calendar.MONTH);
                today.set(Calendar.MONTH,
                    actualMonth == 0
                        ? 11
                        : actualMonth - 1);
            }

            today.set(Calendar.HOUR_OF_DAY, parsed.getHours());
            today.set(Calendar.MINUTE, parsed.getMinutes());
            return observationDate = today.getTime();
        } catch (ParseException e) {
            // shouldn't happen
            throw new RuntimeException(e);
        }
    }
}
