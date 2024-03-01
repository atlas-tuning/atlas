package com.github.manevolent.atlas.uds;

import com.github.manevolent.atlas.Frame;
import com.github.manevolent.atlas.FrameReader;

import java.io.IOException;

public class UDSFrameReader implements FrameReader<UDSFrame> {
    private final FrameReader<?> transport;

    public UDSFrameReader(FrameReader<?> transport) {
        this.transport = transport;
    }

    @Override
    public UDSFrame read() throws IOException {
        Frame frame = transport.read();
        if (frame == null) {
            return null;
        }
        UDSFrame udsFrame = new UDSFrame();
        try {
            udsFrame.read(frame.bitReader());
        } catch (Exception ex) {
            throw new IOException("Problem reading frame " + frame.toHexString(), ex);
        }
        return udsFrame;
    }
}
