package com.github.manevolent.atlas.definition;

import java.nio.ByteOrder;
import java.util.function.BiFunction;

public enum DataFormat {
    SBYTE(1, Precision.WHOLE_NUMBER, (data, byteOrder) -> {
        byte b = data[0];
        return (float) b;
    }, (f, byteOrder) -> {
        f = Math.min(Byte.MAX_VALUE, Math.max(Byte.MIN_VALUE, f));
        int i = (int) Math.floor(f);
        if (i < Byte.MIN_VALUE) {
            i = Byte.MIN_VALUE;
        } else if (i > Byte.MAX_VALUE) {
            i = Byte.MAX_VALUE;
        }
        return new byte[] { (byte) i };
    }),

    UBYTE(1, Precision.WHOLE_NUMBER, (data, byteOrder) -> {
        byte b = data[0];
        int i = b & 0xFF;
        return (float) i;
    }, (f, byteOrder) -> {
        f = Math.min(255, Math.max(0, f));
        int i = (int) Math.floor(f);
        if (i > 0xFF) {
            i = 0xFF;
        } else if (i < 0) {
            i = 0;
        }
        return new byte[] { (byte) i };
    }),

    SSHORT(2, Precision.WHOLE_NUMBER, (data, byteOrder) -> {
        int low, high;
        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            low = data[0] & 0xFF;
            high = data[1] & 0xFF;
        } else {
            high = data[0] & 0xFF;
            low = data[1] & 0xFF;
        }
        int combined = low | (high << 8);
        return (float) (short) (combined & 0xFFFF);
    }, (f, byteOrder) -> {
        f = Math.min(Short.MAX_VALUE, Math.max(Short.MIN_VALUE, f));
        short s = (short)Math.floor(f);
        byte[] data = new byte[2];
        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            data[0] = (byte) (s & 0xFF);
            data[1] = (byte) (s >> 8 & 0xFF);
        } else {
            data[1] = (byte) (s & 0xFF);
            data[0] = (byte) (s >> 8 & 0xFF);
        }
        return data;
    }),

    USHORT(2, Precision.WHOLE_NUMBER, (data, byteOrder) -> {
        int low;
        int high;

        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            low = data[0] & 0xFF;
            high = data[1] & 0xFF;
        } else {
            high = data[0] & 0xFF;
            low = data[1] & 0xFF;
        }

        int combined = low | (high << 8);
        return (float) (combined & 0xFFFF);
    }, (f, byteOrder) -> {
        f = Math.min(65535, Math.max(0, f));
        int s = ((int)Math.floor(f)) & 0xFFFF;
        byte[] data = new byte[2];
        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            data[0] = (byte) (s & 0xFF);
            data[1] = (byte) (s >> 8 & 0xFF);
        } else {
            data[1] = (byte) (s & 0xFF);
            data[0] = (byte) (s >> 8 & 0xFF);
        }
        return data;
    });

    private static byte[] expectLength(byte[] array, int length) {
        if (array.length != length) {
            throw new IllegalArgumentException("Invalid data array size: " +
                    array.length + " != " + length);
        }

        return array;
    }

    private final BiFunction<byte[], ByteOrder, Float> convertFromBytes;
    private final BiFunction<Float, ByteOrder, byte[]> convertToBytes;
    private final int size;
    private final Precision precision;

    DataFormat(int size,
               Precision precision,
               BiFunction<byte[], ByteOrder, Float> convertFromBytes,
               BiFunction<Float, ByteOrder, byte[]> convertToBytes) {
        this.size = size;
        this.convertFromBytes = convertFromBytes;
        this.convertToBytes = convertToBytes;
        this.precision = precision;
    }

    public byte[] convertToBytes(float f, ByteOrder byteOrder) {
        return convertToBytes.apply(f, byteOrder);
    }

    public float convertFromBytes(byte[] data, ByteOrder byteOrder) {
        expectLength(data, getSize());
        return convertFromBytes.apply(data, byteOrder);
    }

    public int getSize() {
        return size;
    }

    public Precision getPrecision() {
        return precision;
    }
}
