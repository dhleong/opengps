package net.dhleong.opengps.modules;

import rx.Observable;

/**
 * @author dhleong
 */
public class NullConnection extends DummyConnection {

    @Override
    public void close() {

    }

    @Override
    public void open() {

    }

    @Override
    public Observable<State> state() {
        return Observable.just(State.DISCONNECTED);
    }

    @Override
    public <T> Observable<T> subscribe(Class<T> type) {
        return Observable.empty();
    }
}
