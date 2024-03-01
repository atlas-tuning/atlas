package com.github.manevolent.atlas;

import java.nio.charset.StandardCharsets;

public interface Frame {

    default BitReader bitReader() {
        byte[] data = getData();
        if (data == null) {
            throw new IllegalArgumentException("no data");
        }

        return new BitReader(data);
    }

    byte[] getData();

    default int getLength() {
        return getData().length;
    }

    default String toAsciiString() {
        return new String(getData(), StandardCharsets.US_ASCII);
    }

    default String toHexString() {
        return toHexString(getData());
    }

    static String toHexString(byte[] data) {
        if (data.length == 0) {
            return "(empty)";
        }

        StringBuilder builder = new StringBuilder(data.length * 2);

        for (byte b : data) {
            String st = String.format("%02X", b);
            builder.append(st);
        }

        return builder.toString();
    }

}
