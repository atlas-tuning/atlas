package com.github.manevolent.atlas.model.source;

import java.io.IOException;

public class ArraySource implements com.github.manevolent.atlas.model.MemorySource {
    private final int offset;
    private final int len;
    private final byte[] data;

    public ArraySource(byte[] data, int offs, int len) {
        this.offset = offs;
        this.len = len;
        this.data = data;
    }

    @Override
    public int read(byte[] dst, long flashOffs, int offs, int len) throws IOException {
        for (int i = 0; i < len && i < offs + len; i ++) {
            dst[offs + i] = data[offs + (int) flashOffs + i];
        }
        return len;
    }

    @Override
    public int read(long flashOffs) throws IOException {
        if (flashOffs < offset || flashOffs >= data.length - len - offset) {
            return -1;
        }

        return data[(int)flashOffs + offset];
    }

    @Override
    public void write(byte[] bytes, long flashOffs, int offs, int len) throws IOException {
        for (int i = 0; i < len && i < offs + len; i ++) {
            data[offs + i] = bytes[offs + (int) flashOffs + i];
        }
    }
}
