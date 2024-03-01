package com.github.manevolent.atlas.isotp;

public class ISOTPFlowControlFrame extends ISOTPSubFrame {
    private final int flag, blockSize, separationTime;

    public ISOTPFlowControlFrame(int flag, int blockSize, int separationTime) {
        this.flag = flag;
        this.blockSize = blockSize;
        this.separationTime = separationTime;
    }

    public int getFlag() {
        return flag;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public int getSeparationTime() {
        return separationTime;
    }
}
