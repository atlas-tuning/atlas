package com.github.manevolent.atlas;

import java.io.IOException;

public interface FrameWriter<T extends Frame> {
    void write(T frame) throws IOException;
}
