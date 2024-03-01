package com.github.manevolent.atlas.can;

import com.github.manevolent.atlas.FrameReader;

import java.io.IOException;

public interface CanFrameReader extends FrameReader<CanFrame> {

    CanFrame read() throws IOException;

}
