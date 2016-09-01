package net.dhleong.opengps;

import net.dhleong.opengps.impl.BaseAeroObject;

import java.util.Collections;
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

    public void appendPointsBetween(AeroObject entry, AeroObject exit, GpsRoute route) {
        appendPointsBetween(entry, exit, route, route.size());
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
    public void appendPointsBetween(AeroObject entry, AeroObject exit, GpsRoute route, int index) {
        int startIndex = points.indexOf(entry);
        int endIndex = points.indexOf(exit);

        int delta = endIndex - startIndex;
        if (delta == 0) {
            // nothing to do
            return;
        }

        final int direction = delta / Math.abs(delta);

        // TODO this ought to be simplifiable...
        if ((index > 0 && route.step(index - 1).ref.equals(entry))
                || (index == 0 && route.size() > 0 && route.step(0).ref.equals(entry))) {
            startIndex += direction;
        }

        if (index < route.size() - 1 && route.step(index + 1).ref.equals(exit)) {
            endIndex -= direction;
        }

        if (endIndex == startIndex) {
            // nothing to do
            return;
        }

        int airwayEntry = index + 1;
        for (int i=startIndex; i != endIndex + direction; i += direction) {
            final AeroObject o = points.get(i);
            if (!(o instanceof NavFix) || (route.flags & GpsRoute.FLAG_INCLUDE_FIXES) != 0) {
                int oldSize = route.size();
                route.add(index, o);
                index += route.size() - oldSize;
            }
        }

        // add airway an exit
        route.add(airwayEntry, this);
        route.add(index + 1, GpsRoute.Step.airwayExit(this));
    }

    public boolean contains(AeroObject obj) {
        return points.contains(obj);
    }

    public List<AeroObject> getPoints() {
        return Collections.unmodifiableList(points);
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
