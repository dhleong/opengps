package net.dhleong.opengps.connection.data;

import net.dhleong.rxconnectr.ConnectrField;

/**
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
}
