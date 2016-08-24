package net.dhleong.opengps;

import rx.Observable;

/**
 * @author dhleong
 */
public interface Storage {
    Observable<Storage> load();

    boolean isLoaded();

    void finishSource(DataSource source);

    boolean hasDataSource(DataSource source);

    void put(Airport airport);
    void put(Navaid navaid);

    void addIlsFrequency(String airportNumber, LabeledFrequency freq);
    void addFrequency(String airportNumber, Airport.FrequencyType type, LabeledFrequency freq);

    void beginTransaction();
    void markTransactionSuccessful();
    void endTransaction();

//    Observable<Airport> airport(String airportId);

    Observable<AeroObject> find(String objectId);

    Observable<AeroObject> findNear(double lat, double lng, float range);
}
