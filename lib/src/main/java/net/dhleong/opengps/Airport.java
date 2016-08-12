package net.dhleong.opengps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author dhleong
 */
public class Airport implements AeroObject {

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
        NAV
    }

    private final Type type;
    private final String number;
    private final String id;

    public final String name;

    private final double lat;
    private final double lng;

    public float elevation;

    private HashMap<FrequencyType, ArrayList<LabeledFrequency>> frequencies = new HashMap<>();

    public Airport(String number, Type type, String id, String name,
            double lat, double lng) {
        this.type = type;
        this.number = number;
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
    public double lat() {
        return lat;
    }

    @Override
    public double lng() {
        return lng;
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

}
