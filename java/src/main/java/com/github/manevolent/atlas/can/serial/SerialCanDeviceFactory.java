package com.github.manevolent.atlas.can.serial;

import com.github.manevolent.atlas.can.CanDeviceDescriptor;
import com.github.manevolent.atlas.can.CanDeviceFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

public class SerialCanDeviceFactory implements CanDeviceFactory {
    @Override
    public Collection<CanDeviceDescriptor> findDevices() {
        return Arrays.stream(Objects.requireNonNull(new File("/dev").listFiles())).filter(deviceFile -> {
            return deviceFile.getName().startsWith("cu");
        }).filter(deviceFile -> {
            // OSX:
            return deviceFile.getName().startsWith("cu.usbmodem");
        }).map(deviceFile -> {
            return new SerialTactrixOpenPort.Descriptor(deviceFile);
        }).collect(Collectors.toList());
    }
}
