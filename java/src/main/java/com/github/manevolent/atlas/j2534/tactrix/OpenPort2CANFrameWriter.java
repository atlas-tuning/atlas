package com.github.manevolent.atlas.j2534.tactrix;

import com.github.manevolent.atlas.Address;
import com.github.manevolent.atlas.can.CANArbitrationId;
import com.github.manevolent.atlas.can.CANFrame;
import com.github.manevolent.atlas.can.CANFrameWriter;

import java.io.IOException;

import java.io.OutputStream;

import java.nio.charset.StandardCharsets;

// Much appreciation for https://github.com/brandonros/rust-tactrix-openport/blob/master/src/lib.rs
public class OpenPort2CANFrameWriter implements CANFrameWriter, AutoCloseable {
    private static final byte channelId = 5;
    private static final int txFlags = 0x00; // CAN_11BIT_ID

    private final OutputStream outputStream;

    public OpenPort2CANFrameWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public void close() throws Exception {
        outputStream.close();
    }

    @Override
    public void write(Address address, CANFrame frame) throws IOException {
        if (frame.getLength() > 8) {
            throw new IllegalArgumentException("Unexpected CAN frame length: " + frame.getLength() + " > 8");
        }

        String command = String.format("att%d %d %d\r\n",
                channelId,
                4 + 8, // 8 bytes CAN + 4 bytes arb ID
                txFlags
        );
        outputStream.write(command.getBytes(StandardCharsets.US_ASCII));

        if (address == null) {
            address = new CANArbitrationId(frame.getArbitrationId());
        }

        outputStream.write(address.getData());

        outputStream.write(frame.getData());

        for (int i = 0; i < 8 - frame.getLength(); i ++) {
            outputStream.write(0x00);
        }

        outputStream.flush();
    }
}
