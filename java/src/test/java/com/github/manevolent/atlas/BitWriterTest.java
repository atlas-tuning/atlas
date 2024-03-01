package com.github.manevolent.atlas;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BitWriterTest {

    @Test
    public void testWrite_bytes() throws IOException {
        byte[] symbols = new byte[] {
                0x00, 0x01, 0x02, 0x03, 0x0C, (byte) 0xC0
        };

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitWriter writer = new BitWriter(baos);
        for (byte symbol : symbols) {
            writer.write(symbol & 0xFF);
        }

        assertArrayEquals(symbols, baos.toByteArray());
    }

    @Test
    public void testWrite_short() throws IOException {
        byte[] symbols = new byte[] {
                0x30, 0x39
        };

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitWriter writer = new BitWriter(baos);
        writer.writeShort((short) 12345);

        assertArrayEquals(symbols, baos.toByteArray());
    }

    @Test
    public void testWrite_int() throws IOException {
        byte[] symbols = new byte[] {
                0x07, 0x5B, (byte)0xCD, 0x15
        };

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitWriter writer = new BitWriter(baos);
        writer.writeInt(123456789);

        assertArrayEquals(symbols, baos.toByteArray());
    }

    @Test
    public void testWrite_nibble() throws IOException {
        byte[] symbols = new byte[] {
                0x0C
        };

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitWriter writer = new BitWriter(baos);
        writer.writeNibble((byte) 0x0);
        writer.writeNibble((byte) 0xC);

        assertArrayEquals(symbols, baos.toByteArray());
    }
}
