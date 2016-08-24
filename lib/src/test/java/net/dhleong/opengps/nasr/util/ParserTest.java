package net.dhleong.opengps.nasr.util;

import net.dhleong.opengps.LabeledFrequency;

import org.junit.Test;

import java.io.IOException;

import static net.dhleong.opengps.test.OkioTest.source;
import static net.dhleong.opengps.test.TestUtil.dmsToDegrees;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author dhleong
 */
public class ParserTest {
    @Test
    public void readFrequency() throws IOException {
        assertThat(parser("118.0  ").frequency())
            .isEqualTo(118);
        assertThat(parser("118.05 ").frequency())
            .isEqualTo(118.05);
        assertThat(parser("118.005").frequency())
            .isEqualTo(118.005);
    }

    @Test
    public void readFrequency6() throws IOException {
        assertThat(parser("118.0 ").frequency6())
            .isEqualTo(118);
        assertThat(parser("118.05").frequency6())
            .isEqualTo(118.05);
        assertThat(parser("118.15").frequency6())
            .isEqualTo(118.15);
    }

    @Test
    public void ilsFrequency() throws IOException {
        assertThat(parser("118.0                                       CD/P                                              ")
            .ilsFrequency()
        ).isEqualTo(new LabeledFrequency("CD/P", 118));
        assertThat(parser("119.9(RWY 02R/20L)                          LCL/P                                             ")
            .ilsFrequency()
        ).isEqualTo(new LabeledFrequency("LCL/P (RWY 02R/20L)", 119.9));
    }

    @Test
    public void ilsFrequencies() throws IOException {
        final String frequencies = "121.8                                       GND/P                                             123.1                                       11AF RESCUE COORD CNTR                            124.3                                       ATIS                                              127.2                                       LCL/P                                             128.8                                       CD/P                                              134.8                                       PTD                                               273.5                                       ATIS                                              275.8                                       GND/P                                             282.8                                       11AF RESCUE COORD CNTR                            121.8                                                       123.1                                                       124.3                                                       127.2                                                       128.8                                                       134.8                                                       273.5                                                       275.8                                                       282.8                                                                                                                                                                                                                                                                             ";
        Parser parser = parser(frequencies);
        assertThat(parser.ilsFrequency())
            .isEqualTo(new LabeledFrequency("GND/P", 121.8));
        assertThat(parser.ilsFrequency())
            .isEqualTo(new LabeledFrequency("11AF RESCUE COORD CNTR", 123.1));
        assertThat(parser.ilsFrequency())
            .isEqualTo(new LabeledFrequency("ATIS", 124.3));
        assertThat(parser.ilsFrequency())
            .isEqualTo(new LabeledFrequency("LCL/P", 127.2));
    }

    @Test
    public void latOrLng_15() throws IOException {
        assertThat(parser("40-46-38.1000N 146798.1000N").latOrLng())
            .isEqualTo(dmsToDegrees(40, 46, 38.1));
    }

    @Test
    public void latOrLng_14() throws IOException {
        assertThat(parser("40-47-01.376N 146821.376N").latOrLng())
            .isEqualTo(dmsToDegrees(40, 47, 1.376));
    }

    public static Parser parser(String string) {
        return Parser.of(source(string));
    }
}
