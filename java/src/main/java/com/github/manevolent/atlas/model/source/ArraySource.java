package com.github.manevolent.atlas.model.source;

import java.io.IOException;

public class ArraySource implements com.github.manevolent.atlas.model.MemorySource {
    private final long memoryBase;
    private final int offset;
    private final int len;
    private final byte[] data;

    public ArraySource(long memoryBaseAddress, byte[] data, int offs, int len) {
        this.memoryBase = memoryBaseAddress;
        this.offset = offs;
        this.len = len;
        this.data = data;
    }

    @Override
    public long getBaseAddress() {
        return memoryBase;
    }

    @Override
    public int getLength() {
        return len;
    }

    @Override
    public int read(byte[] dst, long memoryBase, int offs, int len) throws IOException {
        if (memoryBase + len > this.memoryBase + this.len) {
            throw new ArrayIndexOutOfBoundsException(Long.toString(memoryBase + len));
        } else if (memoryBase < this.memoryBase) {
            throw new ArrayIndexOutOfBoundsException(Long.toString(memoryBase));
        }

        for (int i = 0; i < len; i ++) {
            dst[offs + i] = data[(int) (memoryBase - this.memoryBase) + i + this.offset];
        }
        return len;
    }

    @Override
    public void write(byte[] bytes, long memoryBase, int offs, int len) throws IOException {
        if (memoryBase + len > this.memoryBase + this.len) {
            throw new ArrayIndexOutOfBoundsException(Long.toString(memoryBase + len));
        } else if (memoryBase < this.memoryBase) {
            throw new ArrayIndexOutOfBoundsException(Long.toString(memoryBase));
        }

        for (int i = 0; i < len; i ++) {
            data[(int) (memoryBase - this.memoryBase) + i + this.offset] = bytes[offs + i];
        }
    }

    @Override
    public int read(long position) throws IOException {
        if (position < memoryBase || (this.memoryBase - position) >= len) {
            return -1;
        }

        return data[this.offset + (int) (position - this.memoryBase)];
    }
}
