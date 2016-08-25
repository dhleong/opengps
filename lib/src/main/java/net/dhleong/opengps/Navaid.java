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
    private final String id;
    private final String name;
    private final double lat;
    private final double lng;
    private final double freq;

    public Navaid(Type type, String id, String name, double lat, double lng, double freq) {
        this.type = type;
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.freq = freq;
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

        Navaid navaid = (Navaid) o;

        if (type != navaid.type) return false;
        return id.equals(navaid.id);

    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + id.hashCode();
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
