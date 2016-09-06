package net.dhleong.rxconnectr;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;

import flightsim.simconnect.SimConnect;
import flightsim.simconnect.SimConnectDataType;
import flightsim.simconnect.recv.RecvSimObjectData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author dhleong
 */
public class ReflectiveObjectFactoryTest {

    static class BaseObject {
        @ConnectrField(datumName = "Autopilot Heading Lock Dir", unit = "Degrees")
        float headingBug;
    }

    static class SubObject extends BaseObject {
        @ConnectrField(datumName = "Autopilot Heading Lock", unit = "Bool")
        boolean headingLock;
    }

    static class RadioObject {
        @ConnectrField(datumName = "Transponder Code:1", unit = "BCO16")
        int transponder;

        @ConnectrField(datumName = "Com Active Frequency:1", unit = "Frequency BCD16")
        int com1active;

        @ConnectrField(datumName = "Com Standby Frequency:1", unit = "Frequency BCD16")
        float com1standby;
    }

    SimConnect sc;
    ReflectiveObjectFactory<BaseObject> baseObjectFactory;
    ReflectiveObjectFactory<SubObject> subObjectFactory;

    @Before
    public void setUp() {
        baseObjectFactory = new ReflectiveObjectFactory<>(BaseObject.class);
        subObjectFactory = new ReflectiveObjectFactory<>(SubObject.class);

        sc = mock(SimConnect.class);
    }

    @Test
    public void bind() throws IOException {
        subObjectFactory.bindToDataDefinition(sc, 0);

        verify(sc).addToDataDefinition(0, "Autopilot Heading Lock", "Bool", SimConnectDataType.INT32);
        verify(sc).addToDataDefinition(0, "Autopilot Heading Lock Dir", "Degrees", SimConnectDataType.FLOAT32);
    }

    @Test
    public void create_base() throws IOException {
        baseObjectFactory.bindToDataDefinition(sc, 0);

        ByteBuffer buffer = ByteBuffer.allocate(4)
                                      .putFloat(42f);
        BaseObject obj = baseObjectFactory.create(dataFrom(buffer, 0));

        assertThat(obj.headingBug).isEqualTo(42f);
    }

    @Test
    public void create_subclass() throws IOException {
        subObjectFactory.bindToDataDefinition(sc, 0);

        ByteBuffer buffer = ByteBuffer.allocate(8)
            .putInt(1)
            .putFloat(42f);
        SubObject obj = subObjectFactory.create(dataFrom(buffer, 0));

        assertThat(obj.headingLock).isTrue();
        assertThat(obj.headingBug).isEqualTo(42f);
    }

    @Test
    public void create_radios() throws IOException {
        final ReflectiveObjectFactory<RadioObject> radioFactory =
            new ReflectiveObjectFactory<>(RadioObject.class);
        radioFactory.bindToDataDefinition(sc, 0);

        // NB: these values from RadioUtilTest
        ByteBuffer buffer = ByteBuffer.allocate(12)
                                      .putInt(0x2797)
                                      .putInt(0x2897)
                                      .putInt(0x2797);
        RadioObject obj = radioFactory.create(dataFrom(buffer, 0));

        assertThat(obj.transponder).isEqualTo(2797);
        assertThat(obj.com1active).isEqualTo(128_975);
        assertThat(obj.com1standby).isEqualTo(127.975f);
    }

    static RecvSimObjectData dataFrom(ByteBuffer buffer, int dataId) {
        buffer.flip();

        RecvSimObjectData data = mock(RecvSimObjectData.class);
        when(data.getDefineID()).thenReturn(dataId);
        when(data.getDataFloat32()).thenAnswer(invocation -> buffer.getFloat());
        when(data.getDataFloat64()).thenAnswer(invocation -> buffer.getDouble());
        when(data.getDataInt32()).thenAnswer(invocation -> buffer.getInt());
        when(data.getDataInt64()).thenAnswer(invocation -> buffer.getLong());
        return data;
    }
}