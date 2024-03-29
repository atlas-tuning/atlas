package com.github.manevolent.atlas.protocol.j2534;

import java.io.IOException;
import java.util.List;

public interface J2534DeviceProvider<T extends J2534DeviceDescriptor> {

    /**
     * Gets the default device from this provider, or throws IOException on failure.
     * @return default device.
     */
    default T getDefaultDevice() throws IOException {
        List<T> devices = getAllDevices();

        if (devices == null || devices.isEmpty()) {
            throw new NullPointerException("No J2534 devices were found.");
        }

        return devices.getFirst();
    }

    void setDefaultDevice(J2534DeviceDescriptor descriptor);

    List<T> getAllDevices();

}
