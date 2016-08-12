package net.dhleong.opengps.test;

import java.io.ByteArrayInputStream;

import okio.BufferedSource;
import okio.Okio;

/**
 * @author dhleong
 */
public class OkioTest {
    public static BufferedSource source(String string) {
        return Okio.buffer(Okio.source(new ByteArrayInputStream(string.getBytes())));
    }
}
