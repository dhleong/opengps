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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LabeledFrequency)) return false;

        LabeledFrequency that = (LabeledFrequency) o;

        //noinspection SimplifiableIfStatement
        if (Double.compare(that.frequency, frequency) != 0) return false;
        return label != null ? label.equals(that.label) : that.label == null;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = label != null ? label.hashCode() : 0;
        temp = Double.doubleToLongBits(frequency);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
