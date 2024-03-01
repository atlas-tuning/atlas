package com.github.manevolent.atlas.can;

import java.io.IOException;

import java.io.OutputStream;

import java.nio.charset.StandardCharsets;

// Much appreciation for https://github.com/brandonros/rust-tactrix-openport/blob/master/src/lib.rs
public class OpenPort2FrameWriter implements CanFrameWriter, AutoCloseable {
    private static final byte channelId = 5;
    private static final int txFlags = 0x00;

    private final OutputStream outputStream;

    public OpenPort2FrameWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public void close() throws Exception {
        outputStream.close();
    }

    @Override
    public void write(CanFrame frame) throws IOException {
        if (frame.getLength() > 8) {
            throw new IllegalArgumentException("Unexpected CAN frame length: " + frame.getLength() + " > 8");
        }

        String command = String.format("att%d %d %d\r\n", channelId, frame.getLength(), txFlags);
        outputStream.write(command.getBytes(StandardCharsets.US_ASCII));

        int arbitrationId = frame.getArbitrationId();
        byte[] arbitrationIdBytes = new byte[4];
        arbitrationIdBytes[0] = (byte) ((arbitrationId >> 24) & 0xFF);
        arbitrationIdBytes[1] = (byte) ((arbitrationId >> 16) & 0xFF);
        arbitrationIdBytes[2] = (byte) ((arbitrationId >> 8) & 0xFF);
        arbitrationIdBytes[3] = (byte) ((arbitrationId) & 0xFF);
        outputStream.write(arbitrationIdBytes);

        outputStream.write(frame.getData());

        for (int i = 0; i < 8 - frame.getLength(); i ++) {
            outputStream.write(0x00);
        }

        outputStream.flush();
    }
}
