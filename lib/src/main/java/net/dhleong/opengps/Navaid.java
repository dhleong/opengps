package net.dhleong.opengps;

import net.dhleong.opengps.impl.BaseAeroObject;

/**
 * @author dhleong
 */
public class Navaid extends BaseAeroObject {

    public enum Type {

        VORTAC,
        VORDME("VOR/DME"),
        FAN_MARKER("FAN MARKER"),
        CONSOLAN,
        MARINE_NDB("MARINE NDB"),
        MARINE_NDBDME("MARINE NDB/DME"),
        VOT,
        NDB,
        NDBDME("NDB/DME"),
        TACAN,
        UHFNDB("UHF/NDB"),
        VOR,
        DME;

        private final String asString;

        Type() {
            this(null);
        }
        Type(String asString) {
            this.asString = asString;
        }

        @Override
        public String toString() {
            return asString == null ? name() : asString;
        }

        // single shared array for efficiency
        public static final Type[] VALUES = values();

    }

    private final Type type;
    private final double freq;

    public Navaid(Type type, String id, String name, double lat, double lng, double freq) {
        super(id, name, lat, lng);

        this.type = type;
        this.freq = freq;
    }

    public Type type() {
        return type;
    }

    public double freq() {
        return freq;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Navaid)) return false;
        if (!super.equals(o)) return false;

        Navaid navaid = (Navaid) o;

        return type == navaid.type;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Navaid{" +
            "type=" + type +
            ", id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", freq=" + freq +
            '}';
    }
}
