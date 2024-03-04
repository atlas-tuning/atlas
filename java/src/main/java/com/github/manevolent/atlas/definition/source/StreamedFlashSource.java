package com.github.manevolent.atlas.definition.source;

import com.github.manevolent.atlas.definition.FlashSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.function.Supplier;

public class StreamedFlashSource implements FlashSource {
    private final Supplier<InputStream> supplier;

    public StreamedFlashSource(Supplier<InputStream> supplier) {
        if (supplier == null) {
            throw new IllegalArgumentException(Supplier.class.getSimpleName());
        }

        this.supplier = supplier;
    }

    @Override
    public int read(byte[] dst, int flashOffs, int offs, int len) throws IOException {
        try (InputStream inputStream = supplier.get()) {
            inputStream.skipNBytes(flashOffs);
            return inputStream.read(dst, 0, len);
        }
    }

    @Override
    public int read(int offs) throws IOException {
        try (InputStream inputStream = supplier.get()) {
            assert inputStream.skip(offs) == offs;
            return inputStream.read();
        }
    }

    @Override
    public void write(byte[] bytes, int flashOffs, int offs, int len) throws IOException {
        throw new UnsupportedEncodingException();
    }
}
