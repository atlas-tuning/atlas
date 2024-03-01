package com.github.manevolent.atlas.can;

import java.io.IOException;

public interface CanDevice extends AutoCloseable {

    CanFrameReader reader() throws IOException;
    CanFrameWriter writer() throws IOException;

}
