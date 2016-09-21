package net.dhleong.opengps.feat.charts;

import net.dhleong.opengps.ChartInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * @author dhleong
 */
public class AirportCharts extends HashMap<String, AirportCharts.Result> {

    public static class Result {
        public List<ChartInfo> charts;

        @Override
        public String toString() {
            return Objects.toString(charts);
        }
    }
}
