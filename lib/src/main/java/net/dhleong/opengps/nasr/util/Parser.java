package net.dhleong.opengps.nasr.util;

import net.dhleong.opengps.LabeledFrequency;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import okio.Buffer;
import okio.BufferedSource;
import okio.ByteString;
import okio.Options;

/**
 * @author dhleong
 */
public class Parser {
    static final Options LATLNG15_FORMATTED_END = Options.of(
        ByteString.encodeUtf8("N"),
        ByteString.encodeUtf8("E"),
        ByteString.encodeUtf8("S"),
        ByteString.encodeUtf8("W"),
        ByteString.encodeUtf8(" ")
    );

    static final double SECONDS_TO_DEGREES = 1. / 3600.;

    private final BufferedSource source;

    Map<Class<?>, Options> options = new HashMap<>();
    private boolean isClosed;

    IlsFrequencyWorkspace ilsWorkspace = new IlsFrequencyWorkspace();

    private Parser(BufferedSource source) {
        this.source = source;
    }

    public static Parser of(BufferedSource source) {
        return new Parser(source);
    }

    public void close() throws IOException {
        if (!isClosed) {
            source.close();
            isClosed = true;
        }
    }

    public boolean exhausted() throws IOException {
        return isClosed || source.exhausted();
    }

    public int select(Options options) throws IOException {
        return source.select(options);
    }

    public <T extends Enum> T select(int fieldLength, T[] values) throws IOException {
        final Class<?> type = values[0].getClass();
        final Options cached = options.get(type);
        final Options enumOptions;
        if (cached == null) {
            final int count = values.length;
            ByteString[] byteStringValues = new ByteString[count];

            for (int i=0; i < count; i++) {
                byteStringValues[i] = ByteString.encodeUtf8(values[i].toString());
            }

            enumOptions = Options.of(byteStringValues);
        } else {
            enumOptions = cached;
        }

        final int index = source.select(enumOptions);
        final T result = values[index];
        final int len = result.toString().length();

        source.skip(fieldLength - len);
        return result;
    }

    public void skip(long byteCount) throws IOException {
        source.skip(byteCount);
    }

    public void skipToLineEnd() throws IOException {
        long index = source.indexOf((byte) '\n');
        if (index < 0) {
            // "skip to end" of input
            close();
            return;
        }

        source.skip(index + 1);
    }

    public String string(int maxLength) throws IOException {
        return source.readString(maxLength, Charset.defaultCharset()).trim();
    }

    public float decimalNumber(int byteCount) throws IOException {
        ByteString string = source.readByteString(byteCount);
        return Float.parseFloat(string.utf8().trim());
    }

    /**
     * Reads a formatted + seconds sequence of either lat or lng
     * @throws IOException
     */
    public double latOrLng() throws IOException {

        // it's at least 14 bytes...
        Buffer buffer = ilsWorkspace.typedFreqBuffer;
        buffer.clear();
        source.skip(14);

        // if this selects something, it was 15 bytes;
        //  if not, it was 14, and no harm done
        boolean is15 = -1 != source.select(LATLNG15_FORMATTED_END);

        source.readFully(buffer, is15 ? 12 : 11);

        final long base = buffer.readDecimalLong();
        if ('.' != buffer.readByte()) {
            throw new IllegalStateException("Expected decimal!");
        }
        int decimalPlaces = (int) buffer.size() - 1;
        final long decimal = buffer.readDecimalLong();
        final double value = base + (decimal / Math.pow(10.0, decimalPlaces));

        final byte declination = buffer.readByte();
        final double signedInSeconds;
        if (declination == 'S' || declination == 'W') {
            signedInSeconds = -1. * value;
        } else {
            signedInSeconds = value;
        }

        // now, convert to degrees
        return signedInSeconds * SECONDS_TO_DEGREES;
    }

    /**
     * Reads a single, formatted lat or lng. This is
     *  as opposed to {@link #latOrLng()}, which reads
     *  a pair of formatted and decimal versions of
     *  the same lat/lng.
     */
    public double latOrLngFmt() throws IOException {
        // read into the buffer
        Buffer buffer = ilsWorkspace.labelBuffer;
        buffer.clear();
        source.readFully(buffer, 14);

        final long degrees = buffer.readDecimalLong();
        if (buffer.readByte() != '-') throw new IllegalStateException();

        final long minutes = buffer.readDecimalLong();
        if (buffer.readByte() != '-') throw new IllegalStateException();

        final long seconds = buffer.readDecimalLong();
        if (buffer.readByte() != '.') throw new IllegalStateException();

        final double secondsDecimal = buffer.readDecimalLong() / 1000;
        final double value = degrees + (minutes / 60.) + (seconds / 3600.) + secondsDecimal;

        final byte declination = buffer.readByte();
        if (declination == 'S' || declination == 'W') {
            return -1. * value;
        } else {
            return value;
        }
    }

    public double frequency() throws IOException {
        return addDecimalByte(frequency6(), source.readByte(), 1000);
    }

    // 6-byte frequency
    public double frequency6() throws IOException {

        long baseFreq = source.readDecimalLong();

        if ('.' != source.readByte()) {
            // no decimal; eg: 530
            source.skip(2);
            return baseFreq;
        }

        double decimalPart = addDecimalByte(0, source.readByte(), 10);
        decimalPart = addDecimalByte(decimalPart, source.readByte(), 100);

        return baseFreq + decimalPart;
    }


    public LabeledFrequency ilsFrequency() throws IOException {
        IlsFrequencyWorkspace workspace = ilsWorkspace;
        workspace.typedFreqBuffer.clear();
        workspace.labelBuffer.clear();
        workspace.builder.setLength(0);

        source.readFully(workspace.typedFreqBuffer, 44);
        source.readFully(workspace.labelBuffer, 50);
        if (!workspace.readTrimmed(workspace.labelBuffer)) {
            return null;
        }

        final long decimalPos = workspace.typedFreqBuffer.indexOf((byte) '.');
        long decimalLength = 0;
        for (int i=1; i <= 3; i++, decimalLength++) {
            final byte theByte = workspace.typedFreqBuffer.getByte(decimalPos + decimalLength + 1);
            if (!Character.isDigit((char) theByte)) {
                break;
            }
        }

        final double base = workspace.typedFreqBuffer.readDecimalLong();
        double decimal = 0;
        workspace.typedFreqBuffer.readByte();
        for (int i=0; i < decimalLength; i++) {
            decimal = addDecimalByte(decimal, workspace.typedFreqBuffer.readByte(), Math.pow(10, i+1));
        }

        workspace.readTrimmed(workspace.typedFreqBuffer);

        final double freq = base + decimal;
        return new LabeledFrequency(workspace.builder.toString().trim(), freq);
    }

    private double addDecimalByte(double base, byte readByte, double place) {
        if (readByte <= '0' || readByte > '9') {
            return base;
        }

        int number = readByte - '0';
        return base + (number / place);
    }

    static class IlsFrequencyWorkspace {
        Buffer typedFreqBuffer = new Buffer();
        Buffer labelBuffer = new Buffer();
        StringBuilder builder = new StringBuilder(50);

        /** read from the Buffer into builder, trimming whitespace off the right end */
        boolean readTrimmed(Buffer buffer) throws EOFException {
            long lastTrimmed = buffer.size();
            for (; lastTrimmed > 0; lastTrimmed--) {
                if (buffer.getByte(lastTrimmed - 1) != ' ') {
                    break;
                }
            }

            if (lastTrimmed == 0) return false;

            if (builder.length() > 0 && buffer.getByte(0) != ' ') {
                builder.append(' ');
            }

            builder.append(
                buffer.readString(lastTrimmed, Charset.defaultCharset())
            );

            return true;
        }
    }
}
