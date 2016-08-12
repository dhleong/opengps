package net.dhleong.opengps.nasr.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

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

    private final BufferedSource source;

    Map<Class<?>, Options> options = new HashMap<>();
    private boolean isClosed;

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
        source.skip(14);

        // if this selects something, it was 15 bytes;
        //  if not, it was 14, and no harm done
        source.select(LATLNG15_FORMATTED_END);

        final long base = source.readDecimalLong();
        if ('.' != source.readByte()) {
            throw new IllegalStateException("Expected decimal!");
        }
        final long decimal = source.readDecimalLong();
        final double value = base + (decimal / 10000.0);

        final byte declination = source.readByte();
        if (declination == 'S' || declination == 'W') {
            return -1 * value;
        } else {
            return value;
        }
    }

    public double frequency() throws IOException {
        long baseFreq = source.readDecimalLong();

        if ('.' != source.readByte()) {
            throw new IllegalStateException("Expected decimal!");
        }

        int decimalPart = (int) source.readDecimalLong();
        if (decimalPart < 100) {
            source.skip(1);
            decimalPart *= 10;
        }

        return baseFreq + (decimalPart / 1000.0);
    }
}
