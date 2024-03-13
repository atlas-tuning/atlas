package com.github.manevolent.atlas.protocol.j2534.serial;

import com.github.manevolent.atlas.protocol.j2534.J2534DeviceDescriptor;

import com.github.manevolent.atlas.protocol.j2534.J2534DeviceProvider;
import com.github.manevolent.atlas.protocol.j2534.tactrix.SerialTactrixOpenPort;
import com.github.manevolent.atlas.settings.Setting;
import com.github.manevolent.atlas.settings.Settings;
import com.github.manevolent.atlas.settings.StringValue;

import java.io.File;
import java.util.*;

import java.util.stream.Collectors;

public class UnixSocketTactrixOpenPortProvider implements J2534DeviceProvider<SerialTactrixOpenPort.UnixSocketDescriptor> {
    private static final Setting<StringValue> deviceSetting = new Setting<>(StringValue.class,
            "can.tactrix.serial.unixsocket.file");

    public UnixSocketTactrixOpenPortProvider() {
    }

    @Override
    public SerialTactrixOpenPort.UnixSocketDescriptor getDefaultDevice() {
        List<SerialTactrixOpenPort.UnixSocketDescriptor> unixSocketDescriptors = getAllDevices();

        String desiredDeviceFile = Settings.get(deviceSetting);
        if (desiredDeviceFile != null) {
            Optional<SerialTactrixOpenPort.UnixSocketDescriptor> descriptor =
                    unixSocketDescriptors.stream().filter(x ->x.getDeviceFile().getPath().equals(desiredDeviceFile))
                            .findFirst();

            if (descriptor.isPresent()) {
                return descriptor.get();
            }
        }

        if (unixSocketDescriptors.isEmpty()) {
            return null;
        }

        return unixSocketDescriptors.getFirst();
    }

    @Override
    public void setDefaultDevice(J2534DeviceDescriptor descriptor) {
        if (descriptor instanceof SerialTactrixOpenPort.UnixSocketDescriptor) {
            Settings.set(deviceSetting, ((SerialTactrixOpenPort.UnixSocketDescriptor) descriptor)
                    .getDeviceFile().getPath());
        }
    }

    @Override
    public List<SerialTactrixOpenPort.UnixSocketDescriptor> getAllDevices() {
        File file = new File("/dev");
        if (!file.exists()) {
            return Collections.emptyList();
        }

        return Arrays.stream(Objects.requireNonNull(file.listFiles()))
                .filter(deviceFile -> deviceFile.getName().startsWith("cu"))
                .map(SerialTactrixOpenPort.UnixSocketDescriptor::new)
                .collect(Collectors.toList());
    }
}
