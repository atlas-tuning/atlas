package com.github.manevolent.atlas.isotp;

public class ISOTPFirstFrame extends ISOTPSubFrame {
    private final int totalSize;
    private final byte[] data;

    public ISOTPFirstFrame(int totalSize, byte[] data) {
        this.totalSize = totalSize;
        this.data = data;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public byte[] getData() {
        return data;
    }
}
