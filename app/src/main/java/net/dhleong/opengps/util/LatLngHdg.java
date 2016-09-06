package net.dhleong.opengps.util;

import net.dhleong.rxconnectr.ConnectrField;

/**
 * @author dhleong
 */
public class LatLngHdg {

    @ConnectrField(datumName = "Plane Latitude", unit = "degrees")
    public double lat;

    @ConnectrField(datumName = "Plane Longitude", unit = "degrees")
    public double lng;

    // we *might* actually want magnetic here
    @ConnectrField(datumName = "Plane Heading Degrees True", unit = "degrees")
    public float hdg;
}
