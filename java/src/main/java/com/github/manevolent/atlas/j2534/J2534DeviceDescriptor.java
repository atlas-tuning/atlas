package com.github.manevolent.atlas.j2534;

import com.github.manevolent.atlas.j2534.J2534Device;

import java.io.IOException;

public interface J2534DeviceDescriptor {

    J2534Device createDevice() throws IOException;

}
