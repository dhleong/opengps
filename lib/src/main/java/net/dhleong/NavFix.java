package net.dhleong;

import net.dhleong.opengps.AeroObject;
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

    public class Reference {
        public AeroObject obj;
        public float distance;
        public float bearingFrom;
    }

    List<Reference> refs;

    public NavFix(String id, String name, double lat, double lng, List<Reference> refs) {
        super(id, name, lat, lng);

        this.refs = refs;
    }

    @Override
    public String toString() {
        return "NavFix{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", lat=" + lat +
            ", lng=" + lng +
            "} " + super.toString();
    }
}
