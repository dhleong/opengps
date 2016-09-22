package net.dhleong.opengps.test;

import net.dhleong.opengps.Airport;

import static net.dhleong.opengps.test.TestUtil.dmsToDegrees;

/**
 * @author dhleong
 */
public class Airports {
    public static final Airport LGA = new Airport(
        "15794.*A", Airport.Type.AIRPORT, "KLGA", "LAGUARDIA",
        dmsToDegrees(40, 46, 38.1), dmsToDegrees(-73, -52, -21.4)
    );

    public static final Airport PVD = new Airport(
        "22024.*A", Airport.Type.AIRPORT, "KPVD", "THEODORE FRANCIS GREEN STATE",
        dmsToDegrees(41, 43, 26.397), dmsToDegrees(-71, -25, -41.596)
    );

    public static final Airport SNA = new Airport(
        "02230.*A", Airport.Type.AIRPORT, "KSNA", "JOHN WAYNE",
        dmsToDegrees(33, 40, 32.4), dmsToDegrees(-117, -52, -5.6)
    );

    public static final Airport HHR = new Airport(
        "01647.*A", Airport.Type.AIRPORT, "KHHR", "HAWTHORNE",
        dmsToDegrees(33, 55, 22.223), dmsToDegrees(-118, -20, -6.674)
    );

    static {
        LGA.stateCode = "NY";
        LGA.cityName = "NEW YORK";
        LGA.simpleId = "LGA";

        PVD.simpleId = "PVD";

        SNA.simpleId = "SNA";
        HHR.simpleId = "HHR";
    }
}
