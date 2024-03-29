package com.github.manevolent.atlas.protocol.j2534;

import com.github.manevolent.atlas.FrameReader;
import com.github.manevolent.atlas.FrameWriter;
import com.github.manevolent.atlas.protocol.can.CANFrame;

public interface CANDevice extends AutoCloseable {

    FrameReader<CANFrame> reader();
    FrameWriter<CANFrame> writer();

}
