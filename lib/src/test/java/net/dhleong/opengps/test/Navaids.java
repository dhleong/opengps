package net.dhleong.opengps.test;

import net.dhleong.opengps.Navaid;

import static net.dhleong.opengps.test.TestUtil.dmsToDegrees;

/**
 * Constant navaid objects for testing
 * @author dhleong
 */
public class Navaids {
    public static final Navaid LGA = new Navaid(Navaid.Type.VORDME, "LGA", "LA GUARDIA",
        dmsToDegrees(40, 47, 1.376),
        dmsToDegrees(-73, -52, -6.962),
        0); // we don't care about frequencies...
    public static final Navaid BDR = new Navaid(Navaid.Type.VORDME, "BDR", "BRIDGEPORT",
        dmsToDegrees(41, 9, 38.495),
        dmsToDegrees(-73, -7, -28.188),
        0);
    public static final Navaid MAD = new Navaid(Navaid.Type.VORDME, "MAD", "MADISON",
        dmsToDegrees(41, 18, 49.811),
        dmsToDegrees(-72, -41, -31.893),
        0);
    public static final Navaid ORW = new Navaid(Navaid.Type.VORDME, "ORW", "NORWICH",
        dmsToDegrees(41, 33, 23.053),
        dmsToDegrees(-71, -59, -57.672),
        0);
    public static final Navaid PVD = new Navaid(Navaid.Type.VORTAC, "PVD", "PROVIDENCE",
        dmsToDegrees(41, 43, 27.639),
        dmsToDegrees(-71, -25, -46.699),
        0);

    public static final Navaid HFD = new Navaid(Navaid.Type.VORDME, "HFD", "HARTFORD",
        dmsToDegrees(41, 38, 27.997), dmsToDegrees(-72, -32, -50.705), 0);

    static {
        LGA.magVar = -12;
    }
}
