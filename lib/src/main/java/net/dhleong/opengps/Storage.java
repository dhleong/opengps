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

    void beginTransaction();
    void markTransactionSuccessful();
    void endTransaction();
}
