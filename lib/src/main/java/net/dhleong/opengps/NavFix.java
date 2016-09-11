package net.dhleong.opengps;

import net.dhleong.opengps.impl.BaseAeroObject;

import java.util.List;

/**
 * A NavFix is usually a point on an Airway that is defined
 *  with reference to other {@link AeroObject}s, as opposed to
 *  {@link net.dhleong.opengps.Navaid} which broadcasts its
 *  position in some way.
 *
 * @author dhleong
 */
public class NavFix extends BaseAeroObject {

    public static class Reference {
        public AeroObject obj;
        public float bearingFrom;
        public float distance;

        public Reference(AeroObject obj, float bearingFrom) {
            this(obj, bearingFrom, 0);
        }

        public Reference(AeroObject obj, float bearing, float distance) {
            this.obj = obj;
            this.bearingFrom = bearing;
            this.distance = distance;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Reference)) return false;

            Reference reference = (Reference) o;

            if (Float.compare(reference.distance, distance) != 0) return false;
            if (Float.compare(reference.bearingFrom, bearingFrom) != 0) return false;
            return obj.equals(reference.obj);

        }

        @Override
        public int hashCode() {
            int result = obj.hashCode();
            result = 31 * result + (distance != +0.0f ? Float.floatToIntBits(distance) : 0);
            result = 31 * result + (bearingFrom != +0.0f ? Float.floatToIntBits(bearingFrom) : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Reference{" +
                "obj=" + obj +
                ", distance=" + distance +
                ", bearingFrom=" + bearingFrom +
                '}';
        }
    }

    List<Reference> refs;

    public NavFix(String id, String name, double lat, double lng, List<Reference> refs) {
        super(id, name, lat, lng);

        this.refs = refs;
    }

    public List<Reference> references() {
        return refs;
    }

    @Override
    public String toString() {
        return "NavFix{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", lat=" + lat +
            ", lng=" + lng +
            ", refs=" + refs +
            "}";
    }
}
