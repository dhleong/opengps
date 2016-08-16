package net.dhleong.opengps.storage;

import net.dhleong.opengps.AeroObject;
import net.dhleong.opengps.Airport;
import net.dhleong.opengps.DataSource;
import net.dhleong.opengps.LabeledFrequency;
import net.dhleong.opengps.Navaid;
import net.dhleong.opengps.Storage;

import java.util.HashMap;
import java.util.HashSet;

import rx.Observable;

/**
 * @author dhleong
 */
public class InMemoryStorage implements Storage {

    private HashSet<String> dataSources = new HashSet<>();

    private HashMap<String, Airport> airportsByNumber = new HashMap<>(4096);
    private HashMap<String, Airport> airportsById = new HashMap<>(4096);
    private HashMap<String, Navaid> navaidsById = new HashMap<>(4096);

    @Override
    public Observable<Storage> load() {
        return Observable.just(this);
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    public void finishSource(DataSource source) {
        dataSources.add(source.id());
    }

    @Override
    public boolean hasDataSource(DataSource source) {
        return dataSources.contains(source.id());
    }

    @Override
    public void put(Airport airport) {
        airportsByNumber.put(airport.number(), airport);
        airportsById.put(airport.id(), airport);
    }

    @Override
    public void put(Navaid navaid) {
        navaidsById.put(navaid.id(), navaid);
    }

    @Override
    public void addIlsFrequency(String airportNumber, LabeledFrequency freq) {
        addFrequency(airportNumber, Airport.FrequencyType.NAV, freq);
    }

    @Override
    public void addFrequency(String airportNumber, Airport.FrequencyType type, LabeledFrequency freq) {
        Airport apt = airportsByNumber.get(airportNumber);
        if (apt == null) {
            throw new RuntimeException("No airport known for number " + airportNumber);
        }

        apt.addFrequency(type, freq);
    }

    @Override
    public void beginTransaction() {
        // nop
    }

    @Override
    public void markTransactionSuccessful() {
        // nop
    }

    @Override
    public void endTransaction() {
        // nop
    }

    @Override
    public Observable<AeroObject> find(String objectId) {
        final Navaid navaid = navaidsById.get(objectId);
        final Airport apt = airportsById.get(objectId);

        if (apt == null && navaid == null) {
            return Observable.empty();
        } else if (apt != null && navaid != null) {
            return Observable.just(apt, navaid);
        } else if (apt != null) {
            return Observable.just(apt);
        } else {
            return Observable.just(navaid);
        }
    }
}
