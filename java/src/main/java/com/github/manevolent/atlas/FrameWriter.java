package com.github.manevolent.atlas;

import java.io.IOException;

public interface FrameWriter<F extends Frame> {

    void write(Address address, F frame) throws IOException;

}
