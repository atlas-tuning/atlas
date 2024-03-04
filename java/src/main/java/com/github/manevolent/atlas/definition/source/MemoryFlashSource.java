package com.github.manevolent.atlas.definition.source;

import com.github.manevolent.atlas.definition.FlashSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.function.Supplier;

public class MemoryFlashSource implements FlashSource {
    private byte[] data;

    public MemoryFlashSource(byte[] data) {
        this.data = data;
    }

    @Override
    public int read(byte[] dst, int flashOffs, int offs, int len) throws IOException {
        for (int i = 0; i < len; i ++) {
            dst[offs + i] = data[flashOffs+i];
        }
        return len;
    }

    @Override
    public int read(int flashOffs) throws IOException {
        if (flashOffs < 0 || flashOffs >= data.length) {
            return -1;
        }

        return data[flashOffs];
    }

    @Override
    public void write(byte[] bytes, int flashOffs, int offs, int len) throws IOException {
        for (int i = 0; i < len; i ++) {
            data[flashOffs + i] = bytes[offs + i];
        }
    }

    public static MemoryFlashSource from(InputStream inputStream) throws IOException {
        return new MemoryFlashSource(inputStream.readAllBytes());
    }
}
