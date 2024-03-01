package com.github.manevolent.atlas;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BitReaderTest {

    @Test
    public void testRead_Single() throws IOException {
        // This is the beginning of an ISO-TP packet I got and wasn't
        // right at the time in BitReader.  It's a "first frame" of length 13
        // 0001 0000  0000 1101
        BitReader bitReader = new BitReader(new byte[] { 0x10, 0x0D });

        assertEquals(16, bitReader.remaining());

        assertEquals(0x0, bitReader.read());
        assertEquals(0x0, bitReader.read());
        assertEquals(0x0, bitReader.read());
        assertEquals(0x1, bitReader.read());

        assertEquals(12, bitReader.remaining());

        assertEquals(0x0, bitReader.read());
        assertEquals(0x0, bitReader.read());
        assertEquals(0x0, bitReader.read());
        assertEquals(0x0, bitReader.read());

        assertEquals(8, bitReader.remaining());

        assertEquals(0x0, bitReader.read());
        assertEquals(0x0, bitReader.read());
        assertEquals(0x0, bitReader.read());
        assertEquals(0x0, bitReader.read());

        assertEquals(4, bitReader.remaining());

        assertEquals(0x1, bitReader.read());
        assertEquals(0x1, bitReader.read());
        assertEquals(0x0, bitReader.read());
        assertEquals(0x1, bitReader.read());

        assertEquals(0, bitReader.remaining());
    }

    @Test
    public void testRead_Grouped() throws IOException {
        BitReader bitReader = new BitReader(new byte[] { 0x10, 0x0D });
        int isoTpCode = (int) bitReader.read(4);
        assertEquals(0x1, isoTpCode); // First frame

        int isoTpSize = (int) bitReader.read(12);
        assertEquals(0xD, isoTpSize); // Length = 13 (aka 0xD)
    }

}
