package com.github.manevolent.atlas.can;

import java.io.IOException;

public class BasicCanDevice implements CanDevice {
    private final CanFrameReader reader;
    private final CanFrameWriter writer;

    public BasicCanDevice(CanFrameReader reader, CanFrameWriter writer) {
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public CanFrameReader reader() throws IOException {
        return reader;
    }

    @Override
    public CanFrameWriter writer() throws IOException {
        return writer;
    }

    @Override
    public void close() throws Exception {

    }
}
