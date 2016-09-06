package net.dhleong.opengps.modules;

import net.dhleong.opengps.connection.ConnectionDelegate;
import net.dhleong.opengps.connection.data.RadioData;

import rx.Observable;

/**
 * @author dhleong
 */
public class DummyConnection implements ConnectionDelegate {
    @Override
    public void close() {

    }

    @Override
    public void open() {

    }

    @Override
    public Observable<State> state() {
        return Observable.just(State.CONNECTED);
    }

    @Override
    public <T> Observable<T> subscribe(Class<T> type) {
        if (type == RadioData.class) {
            RadioData data = new RadioData();
            data.com1active = 118.7f;
            data.nav1active = 108.1f;

            //noinspection unchecked
            return Observable.just((T) data);
        }
        return Observable.empty();
    }
}
