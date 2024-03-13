package com.github.manevolent.atlas.protocol.j2534.api;

import com.github.manevolent.atlas.protocol.j2534.J2534Device;
import com.github.manevolent.atlas.protocol.j2534.J2534DeviceDescriptor;

import java.io.IOException;

public class NativeJ2534Descriptor implements J2534DeviceDescriptor {
    @Override
    public J2534Device createDevice() throws IOException {
        return null;
    }
}
