package net.dhleong.opengps.modules;

import net.dhleong.opengps.connection.ConnectionDelegate;
import net.dhleong.opengps.connection.data.RadioData;

import rx.Observable;

/**
 * @author dhleong
 */
public class DummyConnection implements ConnectionDelegate {

    final RadioData radioData = new RadioData();

    public DummyConnection() {
        radioData.com1active = 118.7f;
        radioData.com1standby = 121.35f;
        radioData.nav1active = 108.1f;
        radioData.nav1standby = 111.3f;
        radioData.comTransmit1 = true;
        radioData.comReceiveAll = true;
    }

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

            //noinspection unchecked
            return (Observable<T>) Observable.just(radioData);
        }
        return Observable.empty();
    }

    @Override
    public void swapCom1() {
        // nop
    }

    @Override
    public void swapNav1() {
        // nop
    }

    @Override
    public void setTransmitCom2(boolean transmitCom2) {
        // nop
    }

    @Override
    public void setReceiveAll(boolean receiveAll) {
        // nop
    }

    @Override
    public void setCom1Standby(float frequency) {
        // nop
    }

    @Override
    public void setNav1Standby(float frequency) {
        // nop
    }
}
