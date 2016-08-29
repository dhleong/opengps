package net.dhleong.opengps.impl;

import net.dhleong.opengps.AeroObject;

/**
 * @author dhleong
 */
public abstract class BaseAeroObject implements AeroObject {

    public float magVar;

    protected final String id;
    protected final String name;
    protected final double lat;
    protected final double lng;

    public BaseAeroObject(String id, String name, double lat, double lng) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public double lat() {
        return lat;
    }

    @Override
    public double lng() {
        return lng;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseAeroObject)) return false;

        BaseAeroObject baseAeroObject = (BaseAeroObject) o;

        return id.equals(baseAeroObject.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public float bearingTo(AeroObject other) {
        final double lat1 = Math.toRadians(lat());
        final double lat2 = Math.toRadians(other.lat());
        final double lng1 = Math.toRadians(lng());
        final double lng2 = Math.toRadians(other.lng());

        final double deltaLng = lng2 - lng1;
        final double x = Math.cos(lat2) * Math.sin(deltaLng);
        final double y = (Math.cos(lat1) * Math.sin(lat2))
                - (Math.sin(lat1) * Math.cos(lat2) * Math.cos(deltaLng));

        return (float) Math.toDegrees(Math.atan2(x, y)) - magVar;
    }

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
