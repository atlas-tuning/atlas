package com.github.manevolent.atlas.isotp;

import com.github.manevolent.atlas.FrameReader;
import com.github.manevolent.atlas.can.CANFrame;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ISOTPFrameReader implements FrameReader<ISOTPFrame> {
    private final FrameReader<CANFrame> canReader;
    private final Map<Integer, ISOTPPeer> peers = new HashMap<>();

    public ISOTPFrameReader(FrameReader<CANFrame> canReader) {
        this.canReader = canReader;
    }

    @Override
    public ISOTPFrame read() throws IOException {
        CANFrame canFrame;
        while ((canFrame = canReader.read()) != null) {
            if (canFrame.getLength() <= 0) {
                continue;
            }

            int id = canFrame.getArbitrationId();
            ISOTPPeer peer = peers.computeIfAbsent(id, ISOTPPeer::new);
            ISOTPWireFrame wireFrame = new ISOTPWireFrame();
            wireFrame.read(canFrame.bitReader());
            ISOTPFrame fullFrame = peer.handleFrame(wireFrame.getSubFrame());
            if (fullFrame != null) {
                return fullFrame;
            }
        }

        return null;
    }

    @Override
    public void close() throws Exception {
        canReader.close();
    }
}
