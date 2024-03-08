package com.github.manevolent.atlas.model;

import java.nio.ByteOrder;

public enum MemoryByteOrder {
    BIG_ENDIAN(ByteOrder.BIG_ENDIAN),
    LITTLE_ENDIAN(ByteOrder.LITTLE_ENDIAN);

    private final ByteOrder byteOrder;
    MemoryByteOrder(ByteOrder byteOrder) {
        this.byteOrder = byteOrder;
    }

    public ByteOrder getByteOrder() {
        return byteOrder;
    }
}
