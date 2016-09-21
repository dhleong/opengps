package net.dhleong.opengps;

/**
 * @author dhleong
 */
public class ChartInfo {
    /** The name of the chart */
    public final String name;

    /** The url at which the chart can be found */
    public final String url;

    public ChartInfo(String name, String url) {
        this.name = name;
        this.url = url;
    }

    @Override
    public String toString() {
        return "ChartInfo{" +
            "name='" + name + '\'' +
            ", url='" + url + '\'' +
            '}';
    }
}
