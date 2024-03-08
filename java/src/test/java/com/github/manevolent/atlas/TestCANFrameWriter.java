package com.github.manevolent.atlas;

import com.github.manevolent.atlas.protocol.can.CANFrame;
import com.github.manevolent.atlas.protocol.can.CANFrameWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TestCANFrameWriter implements CANFrameWriter {
    private ByteArrayOutputStream baos = new ByteArrayOutputStream();

    public byte[] getWritten() {
        return baos.toByteArray();
    }

    @Override
    public void write(Address address, CANFrame frame) throws IOException {
        baos.write(frame.getData());
    }
}
