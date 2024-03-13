package com.github.manevolent.atlas.protocol.j2534.usb;

import com.github.manevolent.atlas.protocol.j2534.J2534DeviceDescriptor;
import net.codecrete.usb.UsbDevice;

public abstract class UsbJ2534Descriptor implements J2534DeviceDescriptor {
    private final UsbDevice device;

    protected UsbJ2534Descriptor(UsbDevice device) {
        this.device = device;
    }

    public UsbDevice getDevice() {
        return device;
    }

    public int getDeviceId() {
        int vid_pid = 0x0;
        vid_pid |= device.getProductId();
        vid_pid |= device.getVendorId() << 16;
        return vid_pid;
    }
}
