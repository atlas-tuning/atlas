package com.github.manevolent.atlas.model;

import java.nio.ByteOrder;

public enum MemoryByteOrder {
    BIG_ENDIAN("Big-endian", ByteOrder.BIG_ENDIAN),
    LITTLE_ENDIAN("Little-endian", ByteOrder.LITTLE_ENDIAN);

    private final String name;
    private final ByteOrder byteOrder;
    MemoryByteOrder(String name, ByteOrder byteOrder) {
        this.name = name;
        this.byteOrder = byteOrder;
    }

    public ByteOrder getByteOrder() {
        return byteOrder;
    }


    @Override
    public String toString() {
        return name;
    }
}
