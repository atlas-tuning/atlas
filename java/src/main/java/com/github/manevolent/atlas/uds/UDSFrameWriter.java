package com.github.manevolent.atlas.uds;

import com.github.manevolent.atlas.BasicFrame;
import com.github.manevolent.atlas.Frame;
import com.github.manevolent.atlas.FrameWriter;

import java.io.IOException;

public class UDSFrameWriter implements FrameWriter<UDSFrame> {
    private final FrameWriter<BasicFrame> transport;

    public UDSFrameWriter(FrameWriter<BasicFrame> transport) {
        this.transport = transport;
    }

    @Override
    public void write(UDSFrame frame) throws IOException {
        transport.write(BasicFrame.from(frame));
    }
}
