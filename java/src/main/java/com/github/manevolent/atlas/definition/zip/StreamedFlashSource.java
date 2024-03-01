package com.github.manevolent.atlas.definition.zip;

import com.github.manevolent.atlas.definition.FlashSource;

import java.io.IOException;
import java.io.InputStream;
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
    public int read(byte[] dst, int offs, int len) throws IOException {
        try (InputStream inputStream = supplier.get()) {
            inputStream.skipNBytes(offs);
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
}
