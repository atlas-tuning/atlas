package com.github.manevolent.atlas.protocol.j2534.tactrix;

import com.github.manevolent.atlas.BasicFrame;
import com.github.manevolent.atlas.FrameReader;
import com.github.manevolent.atlas.FrameWriter;
import com.github.manevolent.atlas.protocol.isotp.ISOTPFrame;
import com.github.manevolent.atlas.protocol.j2534.*;
import com.github.manevolent.atlas.protocol.can.CANFrame;
import com.rm5248.serial.NoSuchPortException;
import com.rm5248.serial.NotASerialPortException;
import com.rm5248.serial.SerialPort;
import com.rm5248.serial.SerialPortBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class SerialTactrixOpenPort implements J2534Device {
    private final InputStream is;
    private final OutputStream os;

    public SerialTactrixOpenPort(InputStream is, OutputStream os) {
        this.is = is;
        this.os = new BufferedOutputStream(os);
    }

    private String readLine() throws IOException {
        StringBuilder sb = new StringBuilder();
        int c;

        while ((c = is.read()) >= 0) {
            if (c == '\r') {
                continue;
            }
            if (c == '\n') {
                break;
            }

            sb.append((char)c);
        }

        return sb.toString();
    }

    private void preconnect() throws IOException {
        // Empty the buffer
        int read;
        //noinspection StatementWithEmptyBody
        while (is.available() > 0 && (read = is.read()) >= 0) {
        }

        os.write("\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
        os.write("ati\r\n".getBytes(StandardCharsets.US_ASCII));
        os.flush();

        while (true) {
            String versionInformation = readLine();
            if (versionInformation.startsWith("ari")) {
                break;
            }
        }

        os.write("ata\r\n".getBytes(StandardCharsets.US_ASCII));
        os.flush();
        String answer = readLine();
        if (!answer.equals("aro")) {
            throw new IllegalStateException("Unexpected response: " + answer);
        }
    }

    private void connect(int protocol) throws IOException {
        int flags = 0x00000800; // CAN_ID_BOTH
        int baud = 500_000;
        os.write(String.format("ato%d %d %d 0\r\n",
                        protocol,
                        flags,
                        baud)
                .getBytes(StandardCharsets.US_ASCII));
        os.flush();
        String answer = readLine();
        if (!answer.equals("aro")) {
            throw new IllegalStateException("Unexpected response: " + answer);
        }
    }

    private void setupPassthroughCAN(CANFilter... filters) throws IOException {
        int protocol = 5;

        // PASS_FILTER
        // Allows matching messages into the receive queue. This filter type is only valid on non-ISO 15765 channels
        int filterType = 0x01;

        // ISO15765_FRAME_PAD
        // pad all flow controlled messages to a full CAN frame using zeroes
        int txFlags = 0x00000040;

        int maskSize = 4;

        for (CANFilter filter : filters) {
            os.write(String.format("atf%d %d %d %d\r\n",
                    protocol,
                    filterType,
                    txFlags,
                    maskSize
            ).getBytes(StandardCharsets.US_ASCII));

            os.write(filter.getMask());
            os.write(filter.getPattern());
            os.flush();
        }

        String answer = readLine();
        if (!answer.equals(String.format("arf%d 0 0", protocol))) {
            throw new IllegalStateException("Unexpected response: " + answer);
        }
    }

    private void setupPassthroughISOTP(ISOTPFilter... filters) throws IOException {
        int protocol = 6;

        // FLOW_FILTER
        int filterType = 0x03;

        // ISO15765_FRAME_PAD
        // pad all flow controlled messages to a full CAN frame using zeroes
        int txFlags = 0x00000040;

        int maskSize = 4;

        int index = 0;
        for (ISOTPFilter filter : filters) {
            if (filter.getFlow() == null || filter.getMask() == null || filter.getPattern() == null)
                // Invalid filter, skip it
                continue;

            os.write(String.format("atf%d %d %d %d\r\n",
                    protocol,
                    filterType,
                    txFlags,
                    maskSize
            ).getBytes(StandardCharsets.US_ASCII));

            os.write(filter.getMask());
            os.write(filter.getPattern());
            os.write(filter.getFlow()); // allowed as we set FLOW_FILTER
            os.flush();

            String answer = readLine();
            if (!answer.equals(String.format("arf%d %d %d",
                    protocol, index, J2534Error.STATUS_NOERROR.getCode()))) {
                throw new IllegalStateException("Unexpected response at index " + index + ": " + answer);
            }

            index ++;
        }
    }


    @Override
    public CANDevice openCAN() throws IOException {
        return openCAN(CAN_ALL);
    }

    @Override
    public CANDevice openCAN(CANFilter... filters) throws IOException {
        preconnect();
        connect(5);
        setupPassthroughCAN(filters);

        return new CANDevice() {
            @Override
            public FrameReader<CANFrame> reader() {
                return new OpenPort2CANFrameReader(is);
            }

            @Override
            public FrameWriter<CANFrame> writer() {
                return new OpenPort2CANFrameWriter(os);
            }

            @Override
            public void close() throws Exception {
                is.close();
                os.close();
            }
        };
    }

    @Override
    public ISOTPDevice openISOTOP(ISOTPFilter... filters) throws IOException {
        preconnect();
        connect(6);
        setupPassthroughISOTP(filters);

        return new ISOTPDevice() {
            @Override
            public void close() throws Exception {
                is.close();
                os.close();
            }

            @Override
            public FrameReader<ISOTPFrame> reader() {
                return new OpenPort2ISOTPFrameReader(is);
            }

            @Override
            public FrameWriter<BasicFrame> writer() {
                return new OpenPort2ISOTPFrameWriter(os);
            }
        };
    }

    public enum CommunicationMode {
        SERIAL_DEVICE,
        UNIX_SOCKET
    }

    public static class SerialPortDescriptor implements J2534DeviceDescriptor {
        private final String portName;

        public SerialPortDescriptor(String portName) {
            this.portName = portName;
        }

        @Override
        public String toString() {
            return portName;
        }

        @Override
        public J2534Device createDevice() throws IOException {
            SerialPort serialPort;

            try {
                serialPort = new SerialPortBuilder()
                        .setPort(portName)
                        .setBaudRate(SerialPort.BaudRate.B115200)
                        .build();
            } catch (NoSuchPortException | NotASerialPortException e) {
                throw new IOException(e);
            }

            return new SerialTactrixOpenPort(serialPort.getInputStream(),
                    serialPort.getOutputStream());
        }
    }

    public static class UnixSocketDescriptor implements J2534DeviceDescriptor {
        private final File device;

        public UnixSocketDescriptor(File device) {
            this.device = device;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof UnixSocketDescriptor) {
                return equals((UnixSocketDescriptor) other);
            } else {
                return super.equals(other);
            }
        }

        public boolean equals(UnixSocketDescriptor other) {
            return other.device.getPath().equals(device.getPath());
        }

        public File getDeviceFile() {
            return device;
        }

        @Override
        public String toString() {
            return device.getPath();
        }

        @Override
        public J2534Device createDevice() throws IOException {
            FileInputStream is = new FileInputStream(device);
            FileOutputStream os = new FileOutputStream(device);
            return new SerialTactrixOpenPort(is, os);
        }
    }

}
