package com.github.manevolent.atlas.can;

import java.io.IOException;

public interface CanDeviceDescriptor {

    CanDevice createDevice() throws IOException;

}
