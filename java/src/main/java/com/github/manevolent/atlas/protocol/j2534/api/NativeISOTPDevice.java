package com.github.manevolent.atlas.protocol.j2534.api;

import com.github.manevolent.atlas.BasicFrame;
import com.github.manevolent.atlas.FrameReader;
import com.github.manevolent.atlas.FrameWriter;
import com.github.manevolent.atlas.protocol.isotp.ISOTPFrame;
import com.github.manevolent.atlas.protocol.j2534.ISOTPDevice;

public class NativeISOTPDevice implements ISOTPDevice {
    @Override
    public FrameReader<ISOTPFrame> reader() {
        return null;
    }

    @Override
    public FrameWriter<BasicFrame> writer() {
        return null;
    }

    @Override
    public void close() throws Exception {

    }
}
