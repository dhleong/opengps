package net.dhleong.opengps;

import org.assertj.core.data.Offset;
import org.junit.Test;

import static net.dhleong.opengps.test.Navaids.BDR;
import static net.dhleong.opengps.test.Navaids.LGA;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author dhleong
 */
public class BaseAeroObjectTest {

    @Test
    public void bearing() {
        assertThat(LGA.bearingTo(BDR)).isCloseTo(68, Offset.offset(.2f));
    }
}
