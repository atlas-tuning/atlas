package com.github.manevolent.atlas.protocol.j2534.usb;

import com.github.manevolent.atlas.logging.Log;
import com.github.manevolent.atlas.protocol.j2534.CANDevice;
import com.github.manevolent.atlas.protocol.j2534.ISOTPDevice;
import com.github.manevolent.atlas.protocol.j2534.J2534Device;
import com.github.manevolent.atlas.protocol.j2534.tactrix.SerialTactrixOpenPort;
import com.github.manevolent.atlas.ui.util.Inputs;
import com.sun.jna.Native;
import net.codecrete.usb.UsbDevice;
import net.codecrete.usb.UsbDirection;
import net.codecrete.usb.UsbEndpoint;
import net.codecrete.usb.UsbInterface;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;

public class UsbTactrixOpenPort implements J2534Device {
    private final UsbDevice device;

    public UsbTactrixOpenPort(UsbDevice device) {
        this.device = device;
    }

    private SerialTactrixOpenPort openUsbDevice() {
        if (!device.isOpened()) {
            device.open();
        }

        device.claimInterface(1);
        UsbEndpoint inputEndpoint = device.getEndpoint(UsbDirection.IN, 2);
        UsbEndpoint outputEndpoint = device.getEndpoint(UsbDirection.OUT, 2);
        InputStream inputStream  = device.openInputStream(inputEndpoint.getNumber());
        OutputStream outputStream  = device.openOutputStream(outputEndpoint.getNumber());

        return new SerialTactrixOpenPort(inputStream, outputStream);
    }

    @Override
    public CANDevice openCAN() throws IOException {
        return openUsbDevice().openCAN();
    }

    @Override
    public CANDevice openCAN(CANFilter... filters) throws IOException {
        return openUsbDevice().openCAN(filters);
    }

    @Override
    public ISOTPDevice openISOTOP(ISOTPFilter... filters) throws IOException {
        return openUsbDevice().openISOTOP(filters);
    }

    static class Descriptor extends UsbJ2534Descriptor {
        public Descriptor(UsbDevice device) {
            super(device);
        }

        @Override
        public String toString() {
            return getDevice().toString();
        }

        @Override
        public J2534Device createDevice() {
            return new UsbTactrixOpenPort(getDevice());
        }

    }

}
