package net.dhleong.opengps.ui;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author dhleong
 */
public class IncrementRadioFreqViewTest {

    IncrementRadioFreqView.IncrementRadioFreqState navState;

    @Before
    public void setUp() {
        navState = new IncrementRadioFreqView.NavRadioFreqState();
    }

    @Test
    public void setFrequency() {
        navState.set(132.475f);
        assertThat(navState.chars).containsExactly('1', '3', '2', '.', '4', '7');
        assertThat(navState.asFloat()).isEqualTo(132.475f);
    }

    @Test
    public void navTyping() {
        navState.set(117.45f);

        assertThat(navState.feed(1)).isTrue();
        assertThat(navState.asFloat()).isEqualTo(108.00f);

        assertThat(navState.feed(1)).isTrue();
        assertThat(navState.asFloat()).isEqualTo(110.00f);

        assertThat(navState.feed(7)).isTrue();
        assertThat(navState.asFloat()).isEqualTo(117.00f);

        assertThat(navState.feed(3)).isTrue();
        assertThat(navState.asFloat()).isEqualTo(117.30f);

        assertThat(navState.feed(5)).isTrue();
        assertThat(navState.asFloat()).isEqualTo(117.35f);
    }

    @Test
    public void navTyping_ignoreInvalid() {
        navState.set(108.45f);

        assertThat(navState.feed(2)).isFalse();
        assertThat(navState.asFloat()).isEqualTo(108.45f);

        assertThat(navState.feed(1)).isTrue();
        assertThat(navState.asFloat()).isEqualTo(108.00f);

        assertThat(navState.feed(2)).isFalse();
        assertThat(navState.asFloat()).isEqualTo(108.00f);

        assertThat(navState.feed(0)).isTrue();
        assertThat(navState.asFloat()).isEqualTo(108.00f);

        assertThat(navState.feed(7)).isFalse();
        assertThat(navState.asFloat()).isEqualTo(108.00f);

        assertThat(navState.feed(9)).isTrue();
        assertThat(navState.asFloat()).isEqualTo(109.00f);

        assertThat(navState.feed(5)).isTrue();
        assertThat(navState.asFloat()).isEqualTo(109.50f);

        assertThat(navState.feed(2)).isFalse();
        assertThat(navState.asFloat()).isEqualTo(109.50f);

        assertThat(navState.feed(7)).isFalse();
        assertThat(navState.asFloat()).isEqualTo(109.50f);

        assertThat(navState.feed(5)).isTrue();
        assertThat(navState.asFloat()).isEqualTo(109.55f);
    }

}