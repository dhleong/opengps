package net.dhleong.opengps.storage;

import net.dhleong.opengps.Airport;
import net.dhleong.opengps.DataSource;
import net.dhleong.opengps.Storage;

import java.util.HashSet;

import rx.Observable;

/**
 * @author dhleong
 */
public class InMemoryStorage implements Storage {

    private HashSet<String> dataSources = new HashSet<>();

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

    }
}
