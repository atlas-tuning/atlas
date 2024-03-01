package com.github.manevolent.atlas.isotp;

public class ISOTPConsecutiveFrame extends ISOTPSubFrame {
    private final int index;
    private final byte[] data;

    public ISOTPConsecutiveFrame(int index, byte[] data) {
        this.index = index;
        this.data = data;
    }

    public int getIndex() {
        return index;
    }

    public byte[] getData() {
        return data;
    }
}
