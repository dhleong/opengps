package net.dhleong.opengps.connection;

import net.dhleong.opengps.connection.data.RadioData;
import net.dhleong.opengps.util.LatLngHdg;
import net.dhleong.rxconnectr.RadioUtil;
import net.dhleong.rxconnectr.RxConnectr;

import rx.Observable;

/**
 * @author dhleong
 */
public class SimConnectConnection implements ConnectionDelegate {
    static final String APP_NAME = "opengps";

    final RxConnectr instance;

    public SimConnectConnection(String host, int port) {
        instance = new RxConnectr(APP_NAME, host, port);
        instance.registerObjectType(LatLngHdg.class);
        instance.registerObjectType(RadioData.class);
    }

    @Override
    public void close() {
        instance.close();
    }

    @Override
    public void open() {
        instance.open();
    }

    @Override
    public Observable<State> state() {
        return instance.state().map(simConnectState -> {
            switch (simConnectState) {
            default:
            case DISCONNECTED: return State.DISCONNECTED;
            case CONNECTED: return State.CONNECTED;
            case CONNECTING: return State.CONNECTING;
            }
        });
    }

    @Override
    public <T> Observable<T> subscribe(Class<T> type) {
        return instance.subscribe(type);
    }

    @Override
    public void swapCom1() {
        instance.sendEvent("COM_STBY_RADIO_SWAP", 0);
    }

    @Override
    public void swapNav1() {
        instance.sendEvent("NAV1_RADIO_SWAP", 0);
    }

    @Override
    public void setTransmitCom2(boolean transmitCom2) {
        if (transmitCom2) {
            instance.sendEvent("COM2_TRANSMIT_SELECT", 0);
        } else {
            instance.sendEvent("COM1_TRANSMIT_SELECT", 0);
        }
    }

    @Override
    public void setReceiveAll(boolean receiveAll) {
        instance.sendEvent("COM_RECEIVE_ALL_SET", receiveAll ? 1 : 0);
    }

    @Override
    public void setCom1Standby(float frequency) {
        instance.sendEvent("COM_STBY_RADIO_SET",
            RadioUtil.frequencyAsParam((int) (frequency * 1000)));
    }

    @Override
    public void setNav1Standby(float frequency) {
        instance.sendEvent("NAV1_STBY_SET",
            RadioUtil.frequencyAsParam((int) (frequency * 1000)));
    }

    @Override
    public void setCom1Active(float frequency) {
        instance.sendEvent("COM_RADIO_SET",
            RadioUtil.frequencyAsParam((int) (frequency * 1000)));
    }

    @Override
    public void setNav1Active(float frequency) {
        instance.sendEvent("NAV1_RADIO_SET",
            RadioUtil.frequencyAsParam((int) (frequency * 1000)));
    }
}
