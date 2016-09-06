package net.dhleong.opengps.connection;

import net.dhleong.opengps.connection.data.RadioData;
import net.dhleong.opengps.util.LatLngHdg;
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
    public <T> Observable<T> subscribe(Class<T> type) {
        return instance.subscribe(type);
    }
}
