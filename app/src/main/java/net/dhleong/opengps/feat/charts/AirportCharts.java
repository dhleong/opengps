package net.dhleong.opengps.feat.charts;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * @author dhleong
 */
public class AirportCharts extends HashMap<String, AirportCharts.Result> {
    public static class ChartInfo {
        public String name;
        public String url;

        @Override
        public String toString() {
            return "ChartInfo{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                '}';
        }
    }

    public static class Result {
        public List<ChartInfo> charts;

        @Override
        public String toString() {
            return Objects.toString(charts);
        }
    }
}
