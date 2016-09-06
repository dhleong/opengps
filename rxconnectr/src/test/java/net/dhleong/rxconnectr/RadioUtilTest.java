package net.dhleong.rxconnectr;

import org.junit.Test;

import static net.dhleong.rxconnectr.RadioUtil.frequencyAsParam;
import static net.dhleong.rxconnectr.RadioUtil.paramAsFrequency;
import static net.dhleong.rxconnectr.RadioUtil.paramAsTransponder;
import static net.dhleong.rxconnectr.RadioUtil.transponderAsParam;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author dhleong
 */
public class RadioUtilTest {

    @Test
    public void convertToParam() {
        assertThat(frequencyAsParam(127_975)).isEqualTo(0x2797);
        assertThat(frequencyAsParam(128_975)).isEqualTo(0x2897);
        assertThat(frequencyAsParam(122_450)).isEqualTo(0x2245);
    }

    @Test
    public void convertToFrequency() {
        assertThat(paramAsFrequency(0x2797)).isEqualTo(127_975);
        assertThat(paramAsFrequency(0x2897)).isEqualTo(128_975);
        assertThat(paramAsFrequency(0x2245)).isEqualTo(122_450);
    }

    @Test
    public void convertXpndrToParam() {
        assertThat(transponderAsParam(2797)).isEqualTo(0x2797);
    }

    @Test
    public void convertParamToXpndr() {
        assertThat(paramAsTransponder(0x2797)).isEqualTo(2797);
    }
}