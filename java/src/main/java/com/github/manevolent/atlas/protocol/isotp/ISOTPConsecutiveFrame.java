package com.github.manevolent.atlas.protocol.isotp;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.BitWriter;

import java.io.IOException;

public class ISOTPConsecutiveFrame extends ISOTPDataSubFrame {
    private int index;
    private byte[] data;

    public ISOTPConsecutiveFrame() {
    }

    public ISOTPConsecutiveFrame(int index, byte[] data) {
        this.index = index;
        this.data = data;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void read(BitReader reader) throws IOException {
        this.index = (int) reader.read(4);
        this.data = new byte[7];
        reader.read(this.data);
    }

    public void write(BitWriter writer) throws IOException {
        writer.writeNibble((byte) index);
        writer.write(this.data);
    }

    @Override
    public byte getCode() {
        return 0x2;
    }
}
