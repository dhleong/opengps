package net.dhleong.opengps.storage;

import net.dhleong.opengps.AeroObject;
import net.dhleong.opengps.Airport;
import net.dhleong.opengps.Airway;
import net.dhleong.opengps.ChartInfo;
import net.dhleong.opengps.DataSource;
import net.dhleong.opengps.LabeledFrequency;
import net.dhleong.opengps.NavFix;
import net.dhleong.opengps.Navaid;
import net.dhleong.opengps.Storage;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * A DelegateStorage combines the results of many Storages.
 *  This mostly makes sense when you want to read something
 *  directly from a data source on demand, and/or the Storage
 *  you're using doesn't support one type of data or other.
 *  For example, {@link InMemoryStorage} currently will not
 *  store charts or text SID data, so you could build a
 *  DelegateStorage with special Storage implementations that
 *  call directly to a special DataSource that provides those
 *
 * @author dhleong
 */
public class DelegateStorage implements Storage {

    private final Storage[] storage;

    DelegateStorage(List<Storage> storage) {
        this.storage = storage.toArray(new Storage[storage.size()]);
    }

    @Override
    public Observable<Storage> load() {
        return Observable.from(storage)
            .flatMap(s -> s.load().subscribeOn(Schedulers.io()))
            .last()
            .map(any -> this);
    }

    @Override
    public void finishSource(DataSource source) {
        for (Storage s : storage) {
            s.finishSource(source);
        }
    }

    @Override
    public boolean hasDataSource(DataSource source) {
        for (Storage s : storage) {
            if (s.hasDataSource(source)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void put(Airport airport) {
        //noinspection ForLoopReplaceableByForEach
        for (int i=0, len=storage.length; i < len; i++) {
            storage[i].put(airport);
        }
    }

    @Override
    public void put(Navaid navaid) {
        //noinspection ForLoopReplaceableByForEach
        for (int i=0, len=storage.length; i < len; i++) {
            storage[i].put(navaid);
        }
    }

    @Override
    public void put(NavFix fix) {
        //noinspection ForLoopReplaceableByForEach
        for (int i=0, len=storage.length; i < len; i++) {
            storage[i].put(fix);
        }
    }

    @Override
    public void put(Airway airway) {
        //noinspection ForLoopReplaceableByForEach
        for (int i=0, len=storage.length; i < len; i++) {
            storage[i].put(airway);
        }
    }

    @Override
    public void addIlsFrequency(String airportNumber, LabeledFrequency freq) {
        //noinspection ForLoopReplaceableByForEach
        for (int i=0, len=storage.length; i < len; i++) {
            storage[i].addIlsFrequency(airportNumber, freq);
        }
    }

    @Override
    public void addFrequency(String airportNumber, Airport.FrequencyType type, LabeledFrequency freq) {
        //noinspection ForLoopReplaceableByForEach
        for (int i=0, len=storage.length; i < len; i++) {
            storage[i].addFrequency(airportNumber, type, freq);
        }
    }

    @Override
    public void beginTransaction() {
        //noinspection ForLoopReplaceableByForEach
        for (int i=0, len=storage.length; i < len; i++) {
            storage[i].beginTransaction();
        }
    }

    @Override
    public void markTransactionSuccessful() {
        //noinspection ForLoopReplaceableByForEach
        for (int i=0, len=storage.length; i < len; i++) {
            storage[i].markTransactionSuccessful();
        }
    }

    @Override
    public void endTransaction() {
        //noinspection ForLoopReplaceableByForEach
        for (int i=0, len=storage.length; i < len; i++) {
            storage[i].endTransaction();
        }
    }

    @Override
    public Observable<Airway> airwaysFor(AeroObject object) {
        return Observable.from(storage)
                         .flatMap(s -> s.airwaysFor(object));
    }

    @Override
    public Observable<List<ChartInfo>> chartsFor(Airport airport) {
        return Observable.from(storage)
                         .flatMap(s -> s.chartsFor(airport));
    }

    @Override
    public Observable<AeroObject> find(String objectId) {
        return Observable.from(storage)
                         .flatMap(s -> s.find(objectId));
    }

    @Override
    public Observable<AeroObject> findFix(String fixId) {
        return Observable.from(storage)
                         .flatMap(s -> s.findFix(fixId));
    }

    @Override
    public Observable<AeroObject> findNear(double lat, double lng, float range) {
        return Observable.from(storage)
                         .flatMap(s -> s.findNear(lat, lng, range));
    }

    public static class Builder {

        List<Storage> storage = new ArrayList<>(4);

        public Builder add(Storage storage) {
            this.storage.add(storage);
            return this;
        }

        public Storage build() {
            if (storage.isEmpty()) {
                throw new IllegalStateException("You must provide at least one Storage");
            }

            return new DelegateStorage(storage);
        }
    }
}
