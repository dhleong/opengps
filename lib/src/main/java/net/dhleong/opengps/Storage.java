package net.dhleong.opengps;

import net.dhleong.NavFix;

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
    void put(NavFix fix);

    void addIlsFrequency(String airportNumber, LabeledFrequency freq);
    void addFrequency(String airportNumber, Airport.FrequencyType type, LabeledFrequency freq);

    void beginTransaction();
    void markTransactionSuccessful();
    void endTransaction();

    Observable<AeroObject> find(String objectId);

    /**
     * @param lat In degrees
     * @param lng In degrees
     * @param range Range in nautical miles
     * @return An Observable that emits objects *roughly* within `range`
     *  of the given lat, lng. There may be some items more than `range`
     *  nm away from the given lat-lng, but they won't be very far.
     */
    Observable<AeroObject> findNear(double lat, double lng, float range);
}
