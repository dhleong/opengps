package net.dhleong.opengps.test;

/**
 * @author dhleong
 */
public class TestUtil {
    public static double dmsToDegrees(double degrees, double minutes, double seconds) {
        return degrees + (minutes / 60.) + (seconds / 3600.);
    }
}
