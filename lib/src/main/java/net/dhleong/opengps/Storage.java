package net.dhleong.opengps;

import java.util.List;

import rx.Observable;

/**
 * @author dhleong
 */
public interface Storage {
    Observable<Storage> load();

    void finishSource(DataSource source);

    boolean hasDataSource(DataSource source);

    void put(Airport airport);
    void put(Navaid navaid);
    void put(NavFix fix);
    void put(Airway airway);

    void addIlsFrequency(String airportNumber, LabeledFrequency freq);
    void addFrequency(String airportNumber, Airport.FrequencyType type, LabeledFrequency freq);

    void beginTransaction();
    void markTransactionSuccessful();
    void endTransaction();

    Observable<Airway> airwaysFor(AeroObject object);

    Observable<List<ChartInfo>> chartsFor(Airport airport);

    Observable<AeroObject> find(String objectId);

    /**
     * Convenience version of {@link #find(String)} that returns
     *  the first matching non-Airport
     */
    Observable<AeroObject> findFix(String fixId);

    /**
     * @param lat In degrees
     * @param lng In degrees
     * @param range Range in nautical miles
     * @return An Observable that emits objects *roughly* within `range`
     *  of the given lat, lng. There may be some items more than `range`
     *  nm away from the given lat-lng, but they won't be very far.
     */
    Observable<AeroObject> findNear(double lat, double lng, float range);

    Observable<PreferredRoute> preferredRoutes(Airport origin, Airport dest);
}
