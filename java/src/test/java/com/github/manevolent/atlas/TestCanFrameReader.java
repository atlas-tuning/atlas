package com.github.manevolent.atlas;

import com.github.manevolent.atlas.can.CanFrame;
import com.github.manevolent.atlas.can.CanFrameReader;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class TestCanFrameReader implements CanFrameReader {
    private final InputStream inputStream;

    public TestCanFrameReader(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public CanFrame read() throws IOException {
        byte[] frame = inputStream.readNBytes(8);
        if (frame.length == 0) {
            throw new EOFException();
        }
        return new CanFrame(0x00000000, frame);
    }
}
