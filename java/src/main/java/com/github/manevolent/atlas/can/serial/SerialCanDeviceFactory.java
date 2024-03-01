package com.github.manevolent.atlas.can.serial;

import com.github.manevolent.atlas.can.CanDeviceDescriptor;
import com.github.manevolent.atlas.can.CanDeviceFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

public class SerialCanDeviceFactory implements CanDeviceFactory {
    private final SerialTactrixOpenPort.CommunicationMode communicationMode;

    public SerialCanDeviceFactory(SerialTactrixOpenPort.CommunicationMode communicationMode) {
        this.communicationMode = communicationMode;
    }

    @Override
    public Collection<CanDeviceDescriptor> findDevices() {
        return Arrays.stream(Objects.requireNonNull(new File("/dev").listFiles())).filter(deviceFile -> {
            return deviceFile.getName().startsWith("cu");
        }).filter(deviceFile -> {
            // OSX:
            return deviceFile.getName().startsWith("cu.usbmodem");
        }).map(deviceFile -> new SerialTactrixOpenPort.Descriptor(deviceFile, communicationMode))
                .collect(Collectors.toList());
    }
}
