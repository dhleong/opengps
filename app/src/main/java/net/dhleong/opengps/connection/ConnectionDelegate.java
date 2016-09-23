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

    void swapCom1();
    void swapNav1();

    /**
     * @param transmitCom2 If true, we'll transmit on com2, else com1
     */
    void setTransmitCom2(boolean transmitCom2);
    void setReceiveAll(boolean receiveAll);

    void setCom1Standby(float frequency);
    void setNav1Standby(float frequency);

    void setCom1Active(float freq);
    void setNav1Active(float freq);

}
