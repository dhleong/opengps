package net.dhleong.opengps;

import net.dhleong.opengps.impl.BaseAeroObject;

import java.util.List;

/**
 * @author dhleong
 */
public class Airway extends BaseAeroObject {

    /*
     TODO: airways have a bunch more information:
      - Minimum Enroute Altitude (MEA) at a point
      - Non-standard changeover points between
        navaids (IE: where you should change your radio)
     */

    final List<AeroObject> points;

    public Airway(String id, List<AeroObject> points) {
        super(id, id, points.get(0).lat(), points.get(0).lng()); // I guess?
        this.points = points;
    }

    /**
     * Given valid entry and exit points ON THIS AIRWAY, append all intervening
     *  AeroObjects on this airway to the given route
     * @param entry
     * @param exit
     * @param route
     * @throws IllegalArgumentException if entry or exit are not on this Airway,
     *  or if `route` is null
     */
    public void appendPointsBetween(AeroObject entry, AeroObject exit, GpsRoute route) {
        final int startIndex = points.indexOf(entry);
        final int endIndex = points.indexOf(exit);

        final int direction = (endIndex - startIndex) / Math.abs(endIndex - startIndex);

        for (int i=startIndex; i != endIndex + direction; i += direction) {
            final AeroObject o = points.get(i);
            if (!(o instanceof NavFix) || (route.flags & GpsRoute.FLAG_INCLUDE_FIXES) != 0) {
                route.add(o);
            }
        }
    }

    public boolean contains(AeroObject obj) {
        return points.contains(obj);
    }

    /**
     * Find the nearest entry/exit point on the Airway
     *  to the given AeroObject
     * @param o
     * @return
     */
    public AeroObject nearestTo(AeroObject o) {
        float smallestDistance = Float.MAX_VALUE;
        AeroObject closest = null;

        // TODO since `points` is sorted by route order,
        //  we could potentially short-cut if we find the
        //  distance is increasing; that may be a premature
        //  optimization, though...
        for (int i=0, len=points.size(); i < len; i++) {
            final AeroObject point = points.get(i);
            final float dist = point.distanceTo(o);
            if (dist < smallestDistance) {
                smallestDistance = dist;
                closest = point;
            }
        }

        return closest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Airway)) return false;

        Airway airway = (Airway) o;

        return id.equals(airway.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Airway{" +
            "id='" + id + '\'' +
            ", points=" + points +
            "} " + super.toString();
    }

}
