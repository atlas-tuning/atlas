package com.github.manevolent.atlas.model.source;

import com.github.manevolent.atlas.model.MemorySource;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.function.Supplier;

public class StreamedSource implements MemorySource {
    private final Supplier<InputStream> supplier;

    public StreamedSource(Supplier<InputStream> supplier) {
        if (supplier == null) {
            throw new IllegalArgumentException(Supplier.class.getSimpleName());
        }

        this.supplier = supplier;
    }

    @Override
    public int read(byte[] dst, long flashOffs, int offs, int len) throws IOException {
        try (InputStream inputStream = supplier.get()) {
            inputStream.skipNBytes(flashOffs);
            return inputStream.read(dst, 0, len);
        }
    }

    @Override
    public int read(long offs) throws IOException {
        try (InputStream inputStream = supplier.get()) {
            assert inputStream.skip(offs) == offs;
            return inputStream.read();
        }
    }

    @Override
    public void write(byte[] bytes, long flashOffs, int offs, int len) throws IOException {
        throw new UnsupportedEncodingException();
    }
}
