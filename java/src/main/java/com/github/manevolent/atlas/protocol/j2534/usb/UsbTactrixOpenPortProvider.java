package com.github.manevolent.atlas.protocol.j2534.usb;

import com.github.manevolent.atlas.protocol.j2534.J2534DeviceDescriptor;
import com.github.manevolent.atlas.protocol.j2534.J2534DeviceProvider;
import net.codecrete.usb.USB;
import net.codecrete.usb.USBDevice;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class UsbTactrixOpenPortProvider implements J2534DeviceProvider {
    private static final Map<Integer, Function<USBDevice, J2534DeviceDescriptor>> registry = new HashMap<>();
    static {
        registry.put(0x0403cc4d, UsbTactrixOpenPort.Descriptor::new);
    }

    @Override
    public J2534DeviceDescriptor getDefaultDevice() throws IOException {
        return null;
    }

    @Override
    public void setDefaultDevice(J2534DeviceDescriptor descriptor) {
        
    }

    @Override
    public List<J2534DeviceDescriptor> getAllDevices() {
        List<J2534DeviceDescriptor> descriptors = new ArrayList<>();

        for (var device : USB.getAllDevices()) {
            int vid_pid = 0x0;
            vid_pid |= device.productId();
            vid_pid |= device.vendorId() << 16;
            var constructor = registry.get(vid_pid);
            if (constructor != null) {
                descriptors.add(constructor.apply(device));
            }
        }

        return Collections.unmodifiableList(descriptors);
    }
}
