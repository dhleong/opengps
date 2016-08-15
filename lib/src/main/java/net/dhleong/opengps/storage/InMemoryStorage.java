package net.dhleong.opengps.storage;

import net.dhleong.opengps.Airport;
import net.dhleong.opengps.DataSource;
import net.dhleong.opengps.LabeledFrequency;
import net.dhleong.opengps.Storage;

import java.util.HashMap;
import java.util.HashSet;

import rx.Observable;

/**
 * @author dhleong
 */
public class InMemoryStorage implements Storage {

    private HashSet<String> dataSources = new HashSet<>();

    private HashMap<String, Airport> airportsByNumber = new HashMap<>();
    private HashMap<String, Airport> airportsById = new HashMap<>();


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
    public void addIlsFrequency(String airportNumber, LabeledFrequency freq) {
        addFrequency(airportNumber, Airport.FrequencyType.NAV, freq);
    }

    @Override
    public void addFrequency(String airportNumber, Airport.FrequencyType type, LabeledFrequency freq) {
        Airport apt = airportsByNumber.get(airportNumber);
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
    public Observable<Airport> airport(String airportId) {
        return Observable.just(airportsById.get(airportId));
    }
}
