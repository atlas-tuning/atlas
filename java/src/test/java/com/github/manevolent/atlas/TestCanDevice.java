package com.github.manevolent.atlas;

import com.github.manevolent.atlas.can.CanDevice;
import com.github.manevolent.atlas.can.CanFrameReader;
import com.github.manevolent.atlas.can.CanFrameWriter;

import java.io.IOException;

public class TestCanDevice implements CanDevice {
    private final CanFrameReader reader;
    private final CanFrameWriter writer;

    public TestCanDevice(CanFrameReader reader, CanFrameWriter writer) {
        this.reader = reader;
        this.writer = writer;
    }

    public TestCanDevice(CanFrameReader reader) {
        this.reader = reader;
        this.writer = null;
    }

    public TestCanDevice(CanFrameWriter writer) {
        this.reader = null;
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
        // Do nothing
    }
}
