package com.github.manevolent.atlas;

import com.github.manevolent.atlas.can.CANFrame;
import com.github.manevolent.atlas.can.CANFrameReader;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class TestCanFrameReader implements CANFrameReader {
    private final InputStream inputStream;

    public TestCanFrameReader(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public CANFrame read() throws IOException {
        byte[] frame = inputStream.readNBytes(8);
        if (frame.length == 0) {
            throw new EOFException();
        }
        return new CANFrame(0x00000000, frame);
    }

    @Override
    public void close() throws Exception {
        inputStream.close();
    }
}
