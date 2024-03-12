package com.github.manevolent.atlas.protocol.j2534.serial;

import com.github.manevolent.atlas.protocol.j2534.J2534DeviceDescriptor;

import com.github.manevolent.atlas.protocol.j2534.J2534DeviceProvider;
import com.github.manevolent.atlas.protocol.j2534.tactrix.SerialTactrixOpenPort;
import com.github.manevolent.atlas.settings.Setting;
import com.github.manevolent.atlas.settings.Settings;
import com.github.manevolent.atlas.settings.StringValue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class SerialTactrixOpenPortProvider implements J2534DeviceProvider<SerialTactrixOpenPort.Descriptor> {
    private static final Setting<StringValue> deviceSetting = new Setting<>(StringValue.class,
            "can.tactrix.serial.deviceFile");
    private final SerialTactrixOpenPort.CommunicationMode communicationMode;

    public SerialTactrixOpenPortProvider(SerialTactrixOpenPort.CommunicationMode communicationMode) {
        this.communicationMode = communicationMode;
    }

    @Override
    public SerialTactrixOpenPort.Descriptor getDefaultDevice() {
        List<SerialTactrixOpenPort.Descriptor> descriptors = getAllDevices();

        String desiredDeviceFile = Settings.get(deviceSetting);
        if (desiredDeviceFile != null) {
            Optional<SerialTactrixOpenPort.Descriptor> descriptor =
                    descriptors.stream().filter(x ->x.getDeviceFile().getPath().equals(desiredDeviceFile))
                            .findFirst();

            if (descriptor.isPresent()) {
                return descriptor.get();
            }
        }

        return descriptors.getFirst();
    }

    @Override
    public void setDefaultDevice(SerialTactrixOpenPort.Descriptor descriptor) {
        Settings.set(deviceSetting, descriptor.getDeviceFile().getPath());
    }

    @Override
    public List<SerialTactrixOpenPort.Descriptor> getAllDevices() {
        return Arrays.stream(Objects.requireNonNull(new File("/dev").listFiles()))
                .filter(deviceFile -> deviceFile.getName().startsWith("cu"))
                .filter(deviceFile -> {
                    // OSX:
                    return deviceFile.getName().startsWith("cu.usbmodem");
                })
                .map(deviceFile -> new SerialTactrixOpenPort.Descriptor(deviceFile, communicationMode))
                .collect(Collectors.toList());
    }
}
