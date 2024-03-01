package com.github.manevolent.atlas;

import com.github.manevolent.atlas.can.CanFrame;
import com.github.manevolent.atlas.can.CanFrameWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TestCanFrameWriter implements CanFrameWriter {
    private ByteArrayOutputStream baos = new ByteArrayOutputStream();

    public byte[] getWritten() {
        return baos.toByteArray();
    }

    @Override
    public void write(Address address, CanFrame frame) throws IOException {
        baos.write(frame.getData());
    }
}
