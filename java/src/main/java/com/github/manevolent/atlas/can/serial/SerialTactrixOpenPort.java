package com.github.manevolent.atlas.can.serial;

import com.github.manevolent.atlas.can.CanDevice;
import com.github.manevolent.atlas.can.CanDeviceDescriptor;
import com.rm5248.serial.NoSuchPortException;
import com.rm5248.serial.NotASerialPortException;
import com.rm5248.serial.SerialPort;
import com.rm5248.serial.SerialPortBuilder;

import java.io.File;
import java.io.IOException;

public class SerialTactrixOpenPort implements CanDevice {
    private final SerialPort serialPort;

    public SerialTactrixOpenPort(SerialPort serialPort) {
        this.serialPort = serialPort;
    }



    static class Descriptor implements CanDeviceDescriptor {
        private final File device;

        public Descriptor(File device) {
            this.device = device;
        }

        @Override
        public CanDevice createDevice() {
            try {
                return new SerialTactrixOpenPort(new SerialPortBuilder()
                        .setPort(device.getAbsolutePath())
                        .setBaudRate(SerialPort.BaudRate.B115200)
                        .build());
            } catch (NoSuchPortException e) {
                throw new RuntimeException(e);
            } catch (NotASerialPortException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

}
