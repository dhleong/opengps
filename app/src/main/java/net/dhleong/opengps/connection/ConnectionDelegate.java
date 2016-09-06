package net.dhleong.opengps.connection;

import rx.Observable;

/**
 * @author dhleong
 */
public interface ConnectionDelegate {

    enum State {
        /** Not connected and not trying to */
        DISCONNECTED,

        /** Trying to connect */
        CONNECTING,

        /** Connected! */
        CONNECTED
    }

    void close();
    void open();

    Observable<State> state();

    <T> Observable<T> subscribe(Class<T> type);
}
