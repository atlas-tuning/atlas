package com.github.manevolent.atlas.protocol.j2534.serial;

import com.github.manevolent.atlas.logging.Log;
import com.github.manevolent.atlas.protocol.j2534.J2534DeviceDescriptor;
import com.github.manevolent.atlas.protocol.j2534.J2534DeviceProvider;
import com.github.manevolent.atlas.protocol.j2534.tactrix.SerialTactrixOpenPort;
import com.github.manevolent.atlas.settings.Settings;
import com.rm5248.serial.SerialPort;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class SerialTactrixOpenPortProvider implements J2534DeviceProvider<SerialTactrixOpenPort.SerialPortDescriptor> {
    public SerialTactrixOpenPortProvider() {
    }

    @Override
    public void setDefaultDevice(J2534DeviceDescriptor descriptor) {

    }

    @Override
    public List<SerialTactrixOpenPort.SerialPortDescriptor> getAllDevices() {
        try {
            return Arrays.stream(SerialPort.getSerialPorts())
                    .map(SerialTactrixOpenPort.SerialPortDescriptor::new).toList();
        } catch (IOException e) {
            Log.can().log(Level.WARNING, "Problem getting serial ports", e);
            return Collections.emptyList();
        }
    }
}
