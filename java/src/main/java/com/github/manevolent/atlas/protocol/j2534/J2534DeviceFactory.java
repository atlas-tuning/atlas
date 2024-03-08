package com.github.manevolent.atlas.protocol.j2534;

import java.util.Collection;

public interface J2534DeviceFactory {

    Collection<J2534DeviceDescriptor> findDevices();

}
