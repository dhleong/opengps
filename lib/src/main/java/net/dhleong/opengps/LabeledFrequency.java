package net.dhleong.opengps;

/**
 * @author dhleong
 */
public class LabeledFrequency {
    public final String label;
    public final double frequency;

    public LabeledFrequency(String label, double frequency) {
        this.label = label;
        this.frequency = frequency;
    }

    @Override
    public String toString() {
        return "LabeledFrequency{" +
            "label='" + label + '\'' +
            ", frequency=" + frequency +
            '}';
    }
}
