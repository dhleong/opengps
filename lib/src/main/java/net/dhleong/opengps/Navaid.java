package net.dhleong.opengps;

/**
 * @author dhleong
 */
public class Navaid implements AeroObject {

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
    public String toString() {
        return "Navaid{" +
            "type=" + type +
            ", id='" + id + '\'' +
            ", freq=" + freq +
            '}';
    }
}
