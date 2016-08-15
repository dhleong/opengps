package net.dhleong.opengps.nasr.util;

import net.dhleong.opengps.LabeledFrequency;

import org.junit.Test;

import java.io.IOException;

import static net.dhleong.opengps.test.OkioTest.source;
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
    public void ilsFrequency() throws IOException {
        assertThat(parser("118.0                                       CD/P                                              ")
            .ilsFrequency()
        ).isEqualTo(new LabeledFrequency("CD/P", 118));
        assertThat(parser("119.9(RWY 02R/20L)                          LCL/P                                             ")
            .ilsFrequency()
        ).isEqualTo(new LabeledFrequency("LCL/P (RWY 02R/20L)", 119.9));
    }

    public static Parser parser(String string) {
        return Parser.of(source(string));
    }
}
