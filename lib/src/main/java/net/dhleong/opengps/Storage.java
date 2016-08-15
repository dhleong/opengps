package net.dhleong.opengps;

import rx.Observable;

/**
 * @author dhleong
 */
public interface Storage {
    Observable<Storage> load();

    boolean isLoaded();
    boolean hasDataSource(DataSource source);

    void put(Airport airport);

    void addIlsFrequency(String airportNumber, LabeledFrequency freq);
    void addFrequency(String airportNumber, Airport.FrequencyType type, LabeledFrequency freq);

    void beginTransaction();
    void markTransactionSuccessful();
    void endTransaction();

    Observable<Airport> airport(String airportId);
}
