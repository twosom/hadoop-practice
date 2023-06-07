package com.icloud;

import org.apache.hadoop.io.Text;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringTextComparisonTest {

    @Test
    void string() {
        final String s = "\u0041\u00DF\u6771\uD801\uDC00";
        assertAll(
                () -> assertEquals(s.length(), 5),
                () -> assertEquals(s.getBytes(StandardCharsets.UTF_8).length, 10),
                () -> assertEquals(s.indexOf("\u0041"), 0),
                () -> assertEquals(s.indexOf("\u00DF"), 1),
                () -> assertEquals(s.indexOf("\u6771"), 2),
                () -> assertEquals(s.indexOf("\uD801\uDC00"), 3),
                () -> assertEquals(s.charAt(0), '\u0041'),
                () -> assertEquals(s.charAt(1), '\u00DF'),
                () -> assertEquals(s.charAt(2), '\u6771'),
                () -> assertEquals(s.charAt(3), '\uD801'),
                () -> assertEquals(s.charAt(4), '\uDC00'),
                () -> assertEquals(s.codePointAt(0), 0x0041),
                () -> assertEquals(s.codePointAt(1), 0x00DF),
                () -> assertEquals(s.codePointAt(2), 0x6771),
                () -> assertEquals(s.codePointAt(3), 0x10400)
        );
    }

    @Test
    void text() {
        final Text t = new Text("\u0041\u00DF\u6771\uD801\uDC00");

        assertAll(
                () -> assertEquals(t.find("\u0041"), 0),
                () -> assertEquals(t.find("\u00DF"), 1),
                () -> assertEquals(t.find("\u6771"), 3),
                () -> assertEquals(t.find("\uD801\uDC00"), 6),

                () -> assertEquals(t.charAt(0), 0x0041),
                () -> assertEquals(t.charAt(1), 0x00DF),
                () -> assertEquals(t.charAt(3), 0x6771),
                () -> assertEquals(t.charAt(6), 0x10400)
        );
    }


}
