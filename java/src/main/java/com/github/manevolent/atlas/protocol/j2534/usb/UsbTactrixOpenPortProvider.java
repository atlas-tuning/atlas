package com.github.manevolent.atlas.protocol.j2534.usb;

import com.github.manevolent.atlas.protocol.j2534.J2534DeviceDescriptor;
import com.github.manevolent.atlas.protocol.j2534.J2534DeviceProvider;
import com.github.manevolent.atlas.protocol.j2534.tactrix.SerialTactrixOpenPort;
import com.github.manevolent.atlas.settings.Setting;
import com.github.manevolent.atlas.settings.Settings;
import com.github.manevolent.atlas.settings.StringValue;
import net.codecrete.usb.Usb;
import net.codecrete.usb.UsbDevice;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class UsbTactrixOpenPortProvider implements J2534DeviceProvider<UsbJ2534Descriptor> {
    private static final Setting<StringValue> deviceSetting = new Setting<>(StringValue.class,
            "can.tactrix.usb.device");

    private static final Map<Integer, Function<UsbDevice, UsbJ2534Descriptor>> registry = new HashMap<>();
    static {
        registry.put(0x0403_cc4d, UsbTactrixOpenPort.Descriptor::new);
    }

    @Override
    public UsbJ2534Descriptor getDefaultDevice() throws IOException {
        List<UsbJ2534Descriptor> descriptors = getAllDevices();

        String desiredDeviceFile = Settings.get(deviceSetting);
        if (desiredDeviceFile != null) {
            int deviceId = Integer.decode(desiredDeviceFile);
            Optional<UsbJ2534Descriptor> descriptor = descriptors.stream().filter(x ->
                            x.getDeviceId() == deviceId).findFirst();

            if (descriptor.isPresent()) {
                return descriptor.get();
            }
        }

        if (descriptors.isEmpty()) {
            return null;
        }

        return descriptors.getFirst();
    }

    @Override
    public void setDefaultDevice(J2534DeviceDescriptor descriptor) {
        if (descriptor instanceof UsbJ2534Descriptor) {
            String idString = Integer.toHexString(((UsbJ2534Descriptor) descriptor).getDeviceId());
            Settings.set(deviceSetting, idString);
        }
    }

    @Override
    public List<UsbJ2534Descriptor> getAllDevices() {
        List<UsbJ2534Descriptor> descriptors = new ArrayList<>();

        for (var device : Usb.getDevices()) {
            int vid_pid = 0x0;
            vid_pid |= device.getProductId();
            vid_pid |= device.getVendorId() << 16;
            var constructor = registry.get(vid_pid);
            if (constructor != null) {
                descriptors.add(constructor.apply(device));
            }
        }

        return Collections.unmodifiableList(descriptors);
    }
}
