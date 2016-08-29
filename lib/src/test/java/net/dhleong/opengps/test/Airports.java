package net.dhleong.opengps.test;

import net.dhleong.opengps.Airport;

import static net.dhleong.opengps.test.TestUtil.dmsToDegrees;

/**
 * @author dhleong
 */
public class Airports {
    public static final Airport LGA = new Airport(
        "15794.*A", Airport.Type.AIRPORT, "LGA", "LAGUARDIA",
        dmsToDegrees(40, 46, 38.1), dmsToDegrees(-73, -52, -21.4)
    );

    public static final Airport PVD = new Airport(
        "22024.*A", Airport.Type.AIRPORT, "PVD", "THEODORE FRANCIS GREEN STATE",
        dmsToDegrees(41, 43, 26.397), dmsToDegrees(-71, -25, -41.596)
    );
}
