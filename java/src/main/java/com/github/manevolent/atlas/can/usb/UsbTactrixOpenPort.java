package com.github.manevolent.atlas.can.usb;

import com.github.manevolent.atlas.can.CanDevice;
import com.github.manevolent.atlas.can.CanDeviceDescriptor;
import com.github.manevolent.atlas.can.CanFrameReader;
import com.github.manevolent.atlas.can.CanFrameWriter;
import net.codecrete.usb.USBDevice;
import net.codecrete.usb.USBInterface;

public class UsbTactrixOpenPort implements CanDevice {
    private final USBDevice device;

    public UsbTactrixOpenPort(USBDevice device) {
        this.device = device;
    }

    @Override
    public CanFrameReader reader() {
        throw new UnsupportedOperationException();
    }

    @Override
    public CanFrameWriter writer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws Exception {
        throw new UnsupportedOperationException();
    }

    static class Descriptor implements CanDeviceDescriptor {
        private final USBDevice device;

        public Descriptor(USBDevice device) {
            this.device = device;
        }

        @Override
        public CanDevice createDevice() {
            device.open();

            USBInterface i = device.interfaces().stream().findFirst().orElseThrow();
            device.claimInterface(i.number());

            return new UsbTactrixOpenPort(device);
        }

    }

}
