package com.github.manevolent.atlas.j2534.usb;

import com.github.manevolent.atlas.BasicFrame;
import com.github.manevolent.atlas.FrameReader;
import com.github.manevolent.atlas.FrameWriter;
import com.github.manevolent.atlas.can.*;
import com.github.manevolent.atlas.isotp.ISOTPFrame;
import com.github.manevolent.atlas.j2534.CANDevice;
import com.github.manevolent.atlas.j2534.ISOTPDevice;
import com.github.manevolent.atlas.j2534.J2534Device;
import com.github.manevolent.atlas.j2534.J2534DeviceDescriptor;
import net.codecrete.usb.USBDevice;
import net.codecrete.usb.USBInterface;

import java.io.IOException;

public class UsbTactrixOpenPort implements J2534Device {
    private final USBDevice device;

    public UsbTactrixOpenPort(USBDevice device) {
        this.device = device;
    }

    @Override
    public CANDevice openCAN(CANFilter... filters) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ISOTPDevice openISOTOP(ISOTPFilter... filters) throws IOException {
        throw new UnsupportedOperationException();
    }

    static class Descriptor implements J2534DeviceDescriptor {
        private final USBDevice device;

        public Descriptor(USBDevice device) {
            this.device = device;
        }

        @Override
        public J2534Device createDevice() {
            device.open();

            USBInterface i = device.interfaces().stream().findFirst().orElseThrow();
            device.claimInterface(i.number());

            return new UsbTactrixOpenPort(device);
        }

    }

}
