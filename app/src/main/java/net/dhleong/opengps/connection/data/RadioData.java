package net.dhleong.opengps.connection.data;

import net.dhleong.rxconnectr.ConnectrField;

/**
 * TODO This should *probably* be an interface, or at
 *  least access some things via method, since there's
 *  no guarantee that other connections will be able
 *  to provide everything, or in the same format...
 *
 * @author dhleong
 */
public class RadioData {
    @ConnectrField(datumName = "Transponder Code:1", unit = "BCO16")
    public int transponder;

    @ConnectrField(datumName = "Com Active Frequency:1", unit = "Frequency BCD16")
    public float com1active;

    @ConnectrField(datumName = "Com Standby Frequency:1", unit = "Frequency BCD16")
    public float com1standby;

    @ConnectrField(datumName = "Nav Active Frequency:1", unit = "Frequency BCD16")
    public float nav1active;

    @ConnectrField(datumName = "Nav Standby Frequency:1", unit = "Frequency BCD16")
    public float nav1standby;

    @ConnectrField(datumName = "Com Transmit:1", unit = "Bool")
    public boolean comTransmit1;

    @ConnectrField(datumName = "Com Transmit:2", unit = "Bool")
    public boolean comTransmit2;

    @ConnectrField(datumName = "Com Recieve All", unit = "Bool")
    public boolean comReceiveAll;

}
