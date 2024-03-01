package com.github.manevolent.atlas;

import java.io.IOException;

public interface FrameReader<T extends Frame> {
    T read() throws IOException;
}
