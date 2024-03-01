package com.github.manevolent.atlas.can;

import java.util.Collection;

public interface CanDeviceFactory {

    Collection<CanDeviceDescriptor> findDevices();

}
