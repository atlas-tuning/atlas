package com.github.manevolent.atlas.uds;

import com.github.manevolent.atlas.Addressed;
import com.github.manevolent.atlas.Frame;
import com.github.manevolent.atlas.FrameReader;

import java.io.IOException;

public class UDSFrameReader implements FrameReader<UDSFrame> {
    private final FrameReader<?> transport;
    private final UDSProtocol protocol;

    public UDSFrameReader(FrameReader<?> transport, UDSProtocol protocol) {
        this.transport = transport;
        this.protocol = protocol;
    }

    @Override
    public UDSFrame read() throws IOException {
        Frame frame = transport.read();
        if (frame == null) {
            return null;
        }

        UDSFrame udsFrame = new UDSFrame(protocol);
        if (frame instanceof Addressed) {
            udsFrame.setAddress(((Addressed) frame).getAddress());
        }

        try {
            udsFrame.read(frame.bitReader());
        } catch (Exception ex) {
            throw new IOException("Problem reading frame " + frame.toHexString(), ex);
        }
        return udsFrame;
    }

    @Override
    public void close() throws Exception {
        transport.close();
    }
}
