package net.dhleong.opengps.modules;

import com.jakewharton.rxrelay.BehaviorRelay;

import net.dhleong.opengps.connection.ConnectionDelegate;
import net.dhleong.opengps.connection.data.RadioData;

import rx.Observable;

/**
 * @author dhleong
 */
public class DummyConnection implements ConnectionDelegate {

    final RadioData radioData = new RadioData();
    final BehaviorRelay<RadioData> radioDataRelay = BehaviorRelay.create(radioData);

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
            return (Observable<T>) radioDataRelay;
        }
        return Observable.empty();
    }

    @Override
    public void swapCom1() {
        float oldActive = radioData.com1active;
        radioData.com1active = radioData.com1standby;
        radioData.com1standby = oldActive;
        radioDataRelay.call(radioData);
    }

    @Override
    public void swapNav1() {
        float oldActive = radioData.nav1active;
        radioData.nav1active = radioData.nav1standby;
        radioData.nav1standby = oldActive;
        radioDataRelay.call(radioData);
    }

    @Override
    public void setTransmitCom2(boolean transmitCom2) {
        radioData.comTransmit1 = !transmitCom2;
        radioData.comTransmit2 = transmitCom2;
        radioDataRelay.call(radioData);
    }

    @Override
    public void setReceiveAll(boolean receiveAll) {
        radioData.comReceiveAll = receiveAll;
        radioDataRelay.call(radioData);
    }

}
