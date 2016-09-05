package net.dhleong.rxconnectr;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author dhleong
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConnectrField {

    /**
     * The name of the datum to bind to this field.
     *
     * @see <a href="https://msdn.microsoft.com/en-us/library/cc526981.aspx">Simulation Variables</a>
     */
    String datumName();

    /**
     * The unit to return the variable as
     *
     * @see <a href="https://msdn.microsoft.com/en-us/library/cc526981.aspx#UnitsofMeasurement">Units of Measurement</a>
     */
    String unit();

}
