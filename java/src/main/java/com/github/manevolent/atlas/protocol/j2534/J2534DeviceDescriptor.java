package com.github.manevolent.atlas.protocol.j2534;

import java.io.IOException;

public interface J2534DeviceDescriptor {

    J2534Device createDevice() throws IOException;

}
