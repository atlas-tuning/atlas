package com.github.manevolent.atlas;

import java.io.IOException;

public interface FrameReader<T extends Frame>  extends AutoCloseable {
    T read() throws IOException;
}
