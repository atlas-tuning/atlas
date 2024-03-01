package com.github.manevolent.atlas.definition;

import java.util.function.Function;

public enum DataFormat {
    SBYTE(1, (data) -> {
        byte b = data[0];
        return (float) b;
    }, (f) -> {
        int i = Math.round(f);
        if (i < Byte.MIN_VALUE) {
            i = Byte.MIN_VALUE;
        } else if (i > Byte.MAX_VALUE) {
            i = Byte.MAX_VALUE;
        }
        return new byte[] { (byte) i };
    }),

    UBYTE(1, (data) -> {
        byte b = data[0];
        int i = b & 0xFF;
        return (float) i;
    }, (f) -> {
        int i = Math.round(f);
        if (i > 0xFF) {
            i = 0xFF;
        } else if (i < 0) {
            i = 0;
        }
        return new byte[] { (byte) i };
    }),

    SSHORT(2, (data) -> {
        byte low = data[0];
        byte high = data[1];
        int combined = low | (high << 8);
        return (float) (short) (combined & 0xFFFF);
    }, (f) -> {
        throw new UnsupportedOperationException("TODO");
    }),

    USHORT(2, (data) -> {
        int low = data[0] & 0xFF;
        int high = data[1] & 0xFF;
        int combined = low | (high << 8);
        return (float) (combined & 0xFFFF);
    }, (f) -> {
        throw new UnsupportedOperationException("TODO");
    });

    private static final byte[] expectLength(byte[] array, int length) {
        if (array.length != length) {
            throw new IllegalArgumentException("Invalid data array size: " +
                    array.length + " != " + length);
        }

        return array;
    }

    private final Function<byte[], Float> convertFromBytes;
    private final Function<Float, byte[]> convertToBytes;
    private final int size;

    DataFormat(int size,
               Function<byte[], Float> convertFromBytes,
               Function<Float, byte[]> convertToBytes) {
        this.size = size;
        this.convertFromBytes = convertFromBytes;

        this.convertToBytes = convertToBytes;
    }

    public byte[] convertToBytes(float f) {
        return convertToBytes.apply(f);
    }

    public float convertFromBytes(byte[] data) {
        expectLength(data, getSize());
        return convertFromBytes.apply(data);
    }

    public int getSize() {
        return size;
    }
}
