package com.github.manevolent.atlas.isotp;

import com.github.manevolent.atlas.FrameReader;
import com.github.manevolent.atlas.can.CanFrame;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ISOTPFrameReader implements FrameReader<ISOTPFrame> {
    private final FrameReader<CanFrame> canReader;
    private final Map<Integer, ISOTPPeer> peers = new HashMap<>();

    public ISOTPFrameReader(FrameReader<CanFrame> canReader) {
        this.canReader = canReader;
    }

    @Override
    public ISOTPFrame read() throws IOException {
        CanFrame canFrame;
        while ((canFrame = canReader.read()) != null) {
            int id = canFrame.getArbitrationId();
            ISOTPPeer peer = peers.computeIfAbsent(id, ISOTPPeer::new);
            ISOTPWireFrame wireFrame = new ISOTPWireFrame(canFrame);
            ISOTPFrame fullFrame = peer.handleFrame(wireFrame.getSubFrame());
            if (fullFrame != null) {
                return fullFrame;
            }
        }

        return null;
    }
}
