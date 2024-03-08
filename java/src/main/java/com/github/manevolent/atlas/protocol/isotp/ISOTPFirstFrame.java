package com.github.manevolent.atlas.protocol.isotp;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.BitWriter;
import com.github.manevolent.atlas.Frame;

import java.io.IOException;

public class ISOTPFirstFrame extends ISOTPDataSubFrame implements Frame {
    private int totalSize;
    private byte[] data;

    public ISOTPFirstFrame() {
    }

    @Override
    public void read(BitReader reader) throws IOException {
        this.totalSize = (int) reader.read(12);
        this.data = new byte[6];
        reader.read(data);
    }

    @Override
    public void write(BitWriter writer) throws IOException {
        writer.writeLSB(this.totalSize, 12);

        if (getLength() != 6) {
            throw new IllegalArgumentException("Unexpected data length " + getLength() + " != 6");
        }
        writer.write(getData());
    }

    public int getTotalSize() {
        return totalSize;
    }

    public byte[] getData() {
        return data;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }

    @Override
    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public byte getCode() {
        return 0x1;
    }
}
