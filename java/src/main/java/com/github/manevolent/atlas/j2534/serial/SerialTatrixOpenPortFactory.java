package com.github.manevolent.atlas.j2534.serial;

import com.github.manevolent.atlas.j2534.J2534DeviceDescriptor;
import com.github.manevolent.atlas.j2534.J2534DeviceFactory;
import com.github.manevolent.atlas.j2534.tactrix.SerialTactrixOpenPort;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

public class SerialTatrixOpenPortFactory implements J2534DeviceFactory {
    private final SerialTactrixOpenPort.CommunicationMode communicationMode;

    public SerialTatrixOpenPortFactory(SerialTactrixOpenPort.CommunicationMode communicationMode) {
        this.communicationMode = communicationMode;
    }

    @Override
    public Collection<J2534DeviceDescriptor> findDevices() {
        return Arrays.stream(Objects.requireNonNull(new File("/dev").listFiles())).filter(deviceFile -> {
            return deviceFile.getName().startsWith("cu");
        }).filter(deviceFile -> {
            // OSX:
            return deviceFile.getName().startsWith("cu.usbmodem");
        }).map(deviceFile -> new SerialTactrixOpenPort.Descriptor(deviceFile, communicationMode))
                .collect(Collectors.toList());
    }
}
