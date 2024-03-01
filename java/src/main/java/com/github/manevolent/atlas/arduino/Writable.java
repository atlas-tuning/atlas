package com.github.manevolent.atlas.arduino;

import com.github.manevolent.atlas.BitWriter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public interface Writable {

    void write(Program program, BitWriter writer) throws IOException;

    default void writeAscii(BitWriter writer, String ascii) throws IOException {
        writer.writeShort((short) (ascii.length() & 0xFFFF));
        writer.write(ascii.getBytes(StandardCharsets.US_ASCII));
    }

    default void writeFloatsBE(BitWriter writer, float[] floats) throws IOException {
        for (float f : floats) {
            writer.writeFloatBE(f);
        }
    }

    default void writeFloatsLE(BitWriter writer, float[] floats) throws IOException {
        for (float f : floats) {
            writer.writeFloatLE(f);
        }
    }

}
