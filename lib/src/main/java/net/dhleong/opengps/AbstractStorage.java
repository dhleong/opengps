package net.dhleong.opengps;

import java.util.List;

import rx.Observable;

/**
 * @author dhleong
 */
public abstract class AbstractStorage implements Storage {

    @Override
    public Observable<Storage> load() {
        return Observable.just(this);
    }

    @Override
    public boolean hasDataSource(DataSource source) {
        return false;
    }

    @Override
    public void put(Airport airport) {
        // nop
    }

    @Override
    public void put(Navaid navaid) {
        // nop
    }

    @Override
    public void put(NavFix fix) {
        // nop
    }

    @Override
    public void put(Airway airway) {
        // nop
    }

    @Override
    public void addIlsFrequency(String airportNumber, LabeledFrequency freq) {
        // nop
    }

    @Override
    public void addFrequency(String airportNumber, Airport.FrequencyType type, LabeledFrequency freq) {
        // nop
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
    public Observable<Airway> airwaysFor(AeroObject object) {
        return Observable.empty();
    }

    @Override
    public Observable<List<ChartInfo>> chartsFor(Airport airport) {
        return Observable.empty();
    }

    @Override
    public Observable<AeroObject> find(String objectId) {
        return Observable.empty();
    }

    @Override
    public Observable<AeroObject> findFix(String fixId) {
        return Observable.empty();
    }

    @Override
    public Observable<AeroObject> findNear(double lat, double lng, float range) {
        return Observable.empty();
    }

    @Override
    public Observable<PreferredRoute> preferredRoutes(Airport origin, Airport dest) {
        return Observable.empty();
    }
}
