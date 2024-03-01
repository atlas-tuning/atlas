package com.github.manevolent.atlas;

import com.github.manevolent.atlas.can.CanFrame;
import com.github.manevolent.atlas.can.CanFrameWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TestCanFrameWriter implements CanFrameWriter {
    private ByteArrayOutputStream baos = new ByteArrayOutputStream();

    @Override
    public void write(CanFrame frame) throws IOException {
        baos.write(frame.getData());
    }

    public byte[] getWritten() {
        return baos.toByteArray();
    }
}
