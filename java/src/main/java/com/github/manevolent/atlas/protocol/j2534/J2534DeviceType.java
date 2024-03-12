package com.github.manevolent.atlas.protocol.j2534;

import com.github.manevolent.atlas.protocol.j2534.serial.SerialTactrixOpenPortProvider;
import com.github.manevolent.atlas.protocol.j2534.tactrix.SerialTactrixOpenPort;
import com.github.manevolent.atlas.protocol.j2534.usb.UsbTactrixOpenPortProvider;

import java.util.function.Supplier;

public enum J2534DeviceType {

    TACTRIX_SERIAL(() -> new SerialTactrixOpenPortProvider(
            SerialTactrixOpenPort.CommunicationMode.SERIAL_DEVICE), "Tactrix OpenPort 2.0 (Serial port)"),

    TACTRIX_SERIAL_UNIX_SOCKET(() -> new SerialTactrixOpenPortProvider(
            SerialTactrixOpenPort.CommunicationMode.UNIX_SOCKET), "Tactrix OpenPort 2.0 (Unix socket)"),

    TACTRIX_SERIAL_USB(UsbTactrixOpenPortProvider::new, "Tactrix OpenPort 2.0 (USB)");

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
