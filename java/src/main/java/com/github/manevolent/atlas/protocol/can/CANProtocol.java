package com.github.manevolent.atlas.protocol.can;

import com.github.manevolent.atlas.BitReader;

import java.io.IOException;

public class CANProtocol {
    public static final boolean CAN_BIT_DOMINANT = false;
    public static final boolean CAN_BIT_RECESSIVE = true;

    public static void expect(BitReader reader, boolean expected, String field) throws IOException {
        expect(reader, 1, expected, field);
    }

    public static void expect(BitReader reader, int num, boolean expected, String field) throws IOException {
        for (int i = 0; i < num; i ++) {
            if (reader.readBoolean() != expected) {
                throw new IllegalArgumentException("Unexpected " + field + ": bit #" + (i+1) + " != " + expected);
            }
        }
    }
}
