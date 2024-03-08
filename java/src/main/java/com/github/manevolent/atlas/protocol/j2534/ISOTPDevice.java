package com.github.manevolent.atlas.protocol.j2534;

import com.github.manevolent.atlas.BasicFrame;
import com.github.manevolent.atlas.FrameReader;
import com.github.manevolent.atlas.FrameWriter;
import com.github.manevolent.atlas.protocol.isotp.ISOTPFrame;

public interface ISOTPDevice extends AutoCloseable {

    FrameReader<ISOTPFrame> reader();
    FrameWriter<BasicFrame> writer();

}
