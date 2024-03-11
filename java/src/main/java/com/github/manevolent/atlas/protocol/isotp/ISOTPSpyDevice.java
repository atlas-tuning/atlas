package com.github.manevolent.atlas.protocol.isotp;

import com.github.manevolent.atlas.BasicFrame;
import com.github.manevolent.atlas.FrameReader;
import com.github.manevolent.atlas.FrameWriter;
import com.github.manevolent.atlas.protocol.j2534.ISOTPDevice;

import java.io.UnsupportedEncodingException;

public class ISOTPSpyDevice implements ISOTPDevice {
    private final ISOTPFrameReader frameReader;

    public ISOTPSpyDevice(ISOTPFrameReader frameReader) {
        this.frameReader = frameReader;
    }

    @Override
    public FrameReader<ISOTPFrame> reader() {
        return frameReader;
    }

    @Override
    public FrameWriter<BasicFrame> writer() {
        return (address, frame) -> {
            throw new UnsupportedEncodingException("Device is in spy mode and will not allow " +
                    "writing frames to the CAN bus.");
        };
    }

    @Override
    public void close() throws Exception {
        frameReader.close();
    }
}
