package com.github.manevolent.atlas.protocol.isotp;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.BitWriter;

import java.io.IOException;

public class ISOTPFlowControlFrame extends ISOTPSubFrame {
    private int flag, blockSize, separationTime;

    public ISOTPFlowControlFrame(int flag, int blockSize, int separationTime) {
        this.flag = flag;
        this.blockSize = blockSize;
        this.separationTime = separationTime;
    }

    public ISOTPFlowControlFrame() {

    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public int getSeparationTime() {
        return separationTime;
    }

    public void setSeparationTime(int separationTime) {
        this.separationTime = separationTime;
    }

    @Override
    public byte[] getData() {
        return new byte[0];
    }

    @Override
    public void read(BitReader reader) throws IOException {
        this.flag = (int) reader.read(4);
        this.blockSize = (int) reader.read(8);
        this.separationTime = (int) reader.read(8);
    }

    @Override
    public void write(BitWriter writer) throws IOException {
        writer.writeNibble((byte) this.flag);
        writer.write((byte) this.blockSize);
        writer.write((byte) this.separationTime);
    }

    @Override
    public byte getCode() {
        return 0x3;
    }
}
