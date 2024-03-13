package com.github.manevolent.atlas.protocol.j2534.api;

import com.github.manevolent.atlas.protocol.j2534.J2534DeviceDescriptor;
import com.github.manevolent.atlas.protocol.j2534.J2534DeviceProvider;

import java.io.IOException;
import java.util.List;

public class NativeJ2534Provider implements J2534DeviceProvider<NativeJ2534Descriptor> {
    @Override
    public void setDefaultDevice(J2534DeviceDescriptor descriptor) {

    }

    @Override
    public List<NativeJ2534Descriptor> getAllDevices() {
        return null;
    }
}
