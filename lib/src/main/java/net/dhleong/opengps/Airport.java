package net.dhleong.opengps;

import net.dhleong.opengps.impl.BaseAeroObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author dhleong
 */
public class Airport extends BaseAeroObject {

    public enum Type {
        AIRPORT,
        BALLOONPORT,
        SEAPLANE_BASE("SEAPLANE BASE"),
        GLIDERPORT,
        HELIPORT,
        ULTRALIGHT;

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

    public enum FrequencyType {
        ATIS,
        DELIVERY,
        GROUND,
        TOWER,

        /** Frequencies for navigating, especially ILS */
        NAV,

        /** Misc. frequencies */
        OTHER
    }

    private final Type type;
    private final String number;

    public float elevation;
    public String stateCode;
    public String cityName;

    /** The non-icao ID; used internally in some places */
    public String simpleId;

    private HashMap<FrequencyType, ArrayList<LabeledFrequency>> frequencies = new HashMap<>();

    public Airport(String number, Type type, String id, String name,
            double lat, double lng) {
        super(id, name, lat, lng);

        this.type = type;
        this.number = number;
    }

    public Type airportType() {
        return type;
    }

    public String number() {
        return number;
    }

    public List<LabeledFrequency> frequencies(FrequencyType type) {
        final List<LabeledFrequency> list = frequencies.get(type);
        return list == null ? Collections.emptyList() : list;
    }

    public void addFrequency(FrequencyType type, LabeledFrequency freq) {
        ArrayList<LabeledFrequency> existingList = frequencies.get(type);
        ArrayList<LabeledFrequency> list;
        if (existingList == null) {
            list = new ArrayList<>(4);
            frequencies.put(type, list);
        } else {
            list = existingList;
        }

        list.add(freq);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Airport)) return false;

        Airport airport = (Airport) o;

        if (type != airport.type) return false;
        if (!number.equals(airport.number)) return false;
        return id.equals(airport.id);

    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + number.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Airport{" +
            "type=" + type +
            ", id='" + id + '\'' +
            '}';
    }
}
