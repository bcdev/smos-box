package org.esa.beam.dataio.smos.dddb;


import org.junit.Test;

import java.awt.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TokenParserTest {

    @Test
    public void testParseStringWithDefault() {
        assertEquals("hulperitz", TokenParser.parseString("hulperitz", "schnack"));
        assertEquals("bla", TokenParser.parseString(" bla ", "blo"));

        assertEquals("default", TokenParser.parseString("*", "default"));
        assertEquals("de-fault", TokenParser.parseString(" * ", "de-fault"));
    }

    @Test
    public void testParseString(){
        assertEquals("value", TokenParser.parseString("value"));
        assertEquals("hoppla", TokenParser.parseString(" hoppla "));
    }

    @Test
    public void testParseIntWithDefault() {
        assertEquals(19, TokenParser.parseInt("19", 108));
        assertEquals(20, TokenParser.parseInt(" 20 ", 109));

        assertEquals(110, TokenParser.parseInt("*", 110));
        assertEquals(111, TokenParser.parseInt(" * ", 111));
    }

    @Test
    public void testParseHexWithDefault() {
        assertEquals(33, TokenParser.parseHex("21", 112));
        assertEquals(34, TokenParser.parseHex(" 22 ", 113));

        assertEquals(114, TokenParser.parseHex("*", 114));
        assertEquals(115, TokenParser.parseHex(" * ", 115));
    }

    @Test
    public void testParseDoubleWithDefault() {
        assertEquals(23.4, TokenParser.parseDouble("23.4", 116), 1e-8);
        assertEquals(23.5, TokenParser.parseDouble(" 23.5 ", 117), 1e-8);

        assertEquals(118.9, TokenParser.parseDouble("*", 118.9), 1e-8);
        assertEquals(119.1, TokenParser.parseDouble(" * ", 119.1), 1e-8);
    }

    @Test
    public void testParseColorWithDefault() {
        Color color = TokenParser.parseColor("7766343", Color.BLACK);
        assertEquals("java.awt.Color[r=118,g=99,b=67]", color.toString());

        color = TokenParser.parseColor(" 7766344 ", Color.BLUE);
        assertEquals("java.awt.Color[r=118,g=99,b=68]", color.toString());

        color = TokenParser.parseColor("*", Color.CYAN);
        assertEquals(Color.CYAN, color);

        color = TokenParser.parseColor(" * ", Color.DARK_GRAY);
        assertEquals(Color.DARK_GRAY, color);
    }

    @Test
    public void testParseBooleanWithDefault() {
        assertTrue(TokenParser.parseBoolean("true", false));
        assertFalse(TokenParser.parseBoolean(" false ", true));

        assertTrue(TokenParser.parseBoolean("*", true));
        assertFalse(TokenParser.parseBoolean(" * ", false));
    }
}
