package com.github.manevolent.atlas.protocol.j2534;

import com.github.manevolent.atlas.protocol.j2534.serial.SerialTactrixOpenPortProvider;
import com.github.manevolent.atlas.protocol.j2534.serial.UnixSocketTactrixOpenPortProvider;
import com.github.manevolent.atlas.protocol.j2534.tactrix.SerialTactrixOpenPort;
import com.github.manevolent.atlas.protocol.j2534.usb.UsbTactrixOpenPortProvider;

import java.util.function.Supplier;

public enum J2534DeviceType {

    /**
     * Driver-less OpenPort 2.0 options for folks that don't want to install drivers
     */
    TACTRIX_SERIAL(SerialTactrixOpenPortProvider::new, "Tactrix OpenPort 2.0 (Serial port)"),
    TACTRIX_SERIAL_UNIX_SOCKET(UnixSocketTactrixOpenPortProvider::new, "Tactrix OpenPort 2.0 (Unix socket)");

    private final Supplier<J2534DeviceProvider<?>> provider;
    private final String name;

    J2534DeviceType(Supplier<J2534DeviceProvider<?>> provider, String name) {
        this.provider = provider;
        this.name = name;
    }

    public J2534DeviceProvider<?> getProvider() {
        return provider.get();
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }
}
