package com.github.manevolent.atlas.protocol.j2534.api;

import com.github.manevolent.atlas.protocol.j2534.CANDevice;
import com.github.manevolent.atlas.protocol.j2534.ISOTPDevice;
import com.github.manevolent.atlas.protocol.j2534.J2534Device;
import com.sun.jna.Native;

import java.io.IOException;

public class NativeJ2534Device implements J2534Device {
    private final J2534 device;

    public NativeJ2534Device(String name) {
        this.device = Native.load(name, J2534.class);
    }

    @Override
    public CANDevice openCAN() throws IOException {
        return null;
    }

    @Override
    public CANDevice openCAN(CANFilter... filters) throws IOException {
        return null;
    }

    @Override
    public ISOTPDevice openISOTOP(ISOTPFilter... filters) throws IOException {
        return null;
    }

    public static void main(String[] args) throws IOException {
        new NativeJ2534Device("op20pt32").openISOTOP();
    }
}
