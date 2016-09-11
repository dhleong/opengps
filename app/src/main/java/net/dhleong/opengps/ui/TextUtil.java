package net.dhleong.opengps.ui;

import java.util.Locale;

/**
 * @author dhleong
 */
public class TextUtil {
    public static CharSequence formatLat(double lat) {
        StringBuilder builder = new StringBuilder(12);
        if (lat > 0) {
            builder.append("N ");
        } else {
            builder.append("S ");
        }
        formatLatOrLng(builder, lat);
        return builder;
    }

    public static CharSequence formatLng(double lng) {
        StringBuilder builder = new StringBuilder(12);
        if (lng < 0) {
            builder.append('W');
        } else {
            builder.append("E");
        }
        if (Math.abs(lng) < 100) {
            builder.append('0');
        }
        formatLatOrLng(builder, lng);
        return builder;
    }

    private static void formatLatOrLng(StringBuilder builder, double latOrLng) {
        latOrLng = Math.abs(latOrLng);
        final int degree = (int) Math.floor(latOrLng);
        final double remainder = latOrLng - degree;

        final int minute = (int) Math.floor(60 * remainder);
        final int second = (int) Math.floor(3600 * remainder) - 60 * minute;

        builder.append(degree)
               .append("\u00b0") // degree
               .append(minute)
               .append('.')
               .append(second)
               .append('\'');
    }

    public static CharSequence formatFreq(double freq) {
        return String.format(Locale.US, "%.2f", freq);
    }
}
