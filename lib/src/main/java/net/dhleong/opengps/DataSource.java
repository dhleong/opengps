package net.dhleong.opengps;

import rx.Observable;

/**
 * @author dhleong
 */
public interface DataSource {
    String id();

    Observable<Boolean> loadInto(Storage storage);
}
