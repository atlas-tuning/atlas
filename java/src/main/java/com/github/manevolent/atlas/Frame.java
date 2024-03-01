package com.github.manevolent.atlas;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public interface Frame {

    default BitReader bitReader() {
        byte[] data = getData();
        if (data == null) {
            throw new IllegalArgumentException("no data");
        }

        return new BitReader(data);
    }

    default byte[] getData() {
        throw new UnsupportedOperationException(getClass().getName() + " does not support getData()");
    }

    default void read(BitReader reader) throws IOException {
        throw new UnsupportedOperationException(getClass().getName() + " does not support read()");
    }

    default void write(BitWriter writer) throws IOException {
        throw new UnsupportedOperationException(getClass().getName() + " does not support write()");
    }

    default byte[] write() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitWriter writer = new BitWriter(baos);
        write(writer);
        return baos.toByteArray();
    }

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
        if (data == null) {
            return "(null)";
        } else if (data.length == 0) {
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
