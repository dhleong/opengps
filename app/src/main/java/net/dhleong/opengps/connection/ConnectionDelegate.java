package net.dhleong.opengps.connection;

import rx.Observable;

/**
 * @author dhleong
 */
public interface ConnectionDelegate {

    void close();
    void open();

    <T> Observable<T> subscribe(Class<T> type);
}
