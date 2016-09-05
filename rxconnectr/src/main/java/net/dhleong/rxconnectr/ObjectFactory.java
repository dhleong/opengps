package net.dhleong.rxconnectr;

import java.io.IOException;

import flightsim.simconnect.SimConnect;
import flightsim.simconnect.recv.RecvSimObjectData;

/**
 * @author dhleong
 */
public interface ObjectFactory<T> {
    T create(RecvSimObjectData data);

    void bindToDataDefinition(SimConnect connect, int dataId) throws IOException;

    /** @return whatever was passed from {@link #bindToDataDefinition(SimConnect, int)} */
    int getDataId();
}
