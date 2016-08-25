package net.dhleong.opengps.impl;

import net.dhleong.opengps.AeroObject;

/**
 * @author dhleong
 */
public abstract class BaseAeroObject implements AeroObject {

    @Override
    public float distanceTo(AeroObject other) {
        // haversine function, adapted from:
        //  http://www.movable-type.co.uk/scripts/latlong.html

        final double lat1 = lat();
        final double lat2 = other.lat();
        final double lng1 = lng();
        final double lng2 = other.lng();

        final double phi1 = Math.toRadians(lat1);
        final double phi2 = Math.toRadians(lat2);

        final double deltaPhi = Math.toRadians(lat2 - lat1);
        final double deltaLambda = Math.toRadians(lng2 - lng1);

        final double deltaPhi2sin = Math.sin(deltaPhi / 2.);
        final double deltaLam2sin = Math.sin(deltaLambda / 2.);

        final double a = deltaPhi2sin * deltaPhi2sin
            + Math.cos(phi1) * Math.cos(phi2)
                * deltaLam2sin * deltaLam2sin;
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (float) (Const.EARTH_RADIUS_KM * c * Const.KM_TO_NM);
    }

}
