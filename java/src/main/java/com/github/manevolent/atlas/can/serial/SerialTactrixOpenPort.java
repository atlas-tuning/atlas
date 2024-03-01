package com.github.manevolent.atlas.can.serial;

import com.github.manevolent.atlas.can.*;
import com.rm5248.serial.NoSuchPortException;
import com.rm5248.serial.NotASerialPortException;
import com.rm5248.serial.SerialPort;
import com.rm5248.serial.SerialPortBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class SerialTactrixOpenPort implements CanDevice {
    private final InputStream is;
    private final OutputStream os;

    private boolean initialized;

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

    private void ensureInitialized() throws IOException {
        synchronized (this) {
            if (!initialized) {
                os.write("\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
                os.write("ati\r\n".getBytes(StandardCharsets.US_ASCII));
                os.flush();
                String versionInformation = readLine();
                if (!versionInformation.startsWith("ari")) {
                    throw new IllegalStateException("Unexpected response: " + versionInformation);
                }

                os.write("ata\r\n".getBytes(StandardCharsets.US_ASCII));
                os.flush();
                String answer = readLine();
                if (!answer.equals("aro")) {
                    throw new IllegalStateException("Unexpected response: " + answer);
                }

                int channelId = 5;
                int flags = 0x00000800;
                int baud = 500_000;
                os.write(String.format("ato%d %d %d 0\r\n",
                                channelId,
                                flags,
                                baud)
                        .getBytes(StandardCharsets.US_ASCII));
                os.flush();
                answer = readLine();
                if (!answer.equals("aro")) {
                    throw new IllegalStateException("Unexpected response: " + answer);
                }

                os.write(String.format("atf%d 1 64 4\r\n", channelId)
                        .getBytes(StandardCharsets.US_ASCII));

                byte[] mask = new byte[] { 0x00, 0x00, 0x00, 0x00 };
                byte[] pattern = new byte[] { 0x00, 0x00, 0x00, 0x00 };
                os.write(mask);
                os.write(pattern);
                os.flush();

                answer = readLine();
                if (!answer.equals(String.format("arf%d 0 0", channelId))) {
                    throw new IllegalStateException("Unexpected response: " + answer);
                }

                initialized = true;
            }
        }
    }

    @Override
    public CanFrameReader reader() throws IOException {
        ensureInitialized();
        return new OpenPort2FrameReader(is);
    }

    @Override
    public CanFrameWriter writer() throws IOException {
        ensureInitialized();
        return new OpenPort2FrameWriter(os);
    }

    @Override
    public void close() throws Exception {
        is.close();
        os.close();
    }

    public enum CommunicationMode {
        SERIAL_DEVICE,
        DIRECT_SOCKET
    }

    static class Descriptor implements CanDeviceDescriptor {
        private final File device;
        private final CommunicationMode communicationMode;

        public Descriptor(File device, CommunicationMode communicationMode) {
            this.device = device;
            this.communicationMode = communicationMode;
        }

        @Override
        public CanDevice createDevice() throws IOException {
            switch (communicationMode) {
                case SERIAL_DEVICE:
                    try {
                        SerialPort serialPort = new SerialPortBuilder()
                                .setPort(device.getAbsolutePath())
                                .setBaudRate(SerialPort.BaudRate.B115200)
                                .build();

                        return new SerialTactrixOpenPort(serialPort.getInputStream(),
                                serialPort.getOutputStream());
                    } catch (NoSuchPortException e) {
                        throw new RuntimeException(e);
                    } catch (NotASerialPortException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                case DIRECT_SOCKET:
                    FileInputStream is = new FileInputStream(device);
                    FileOutputStream os = new FileOutputStream(device);
                    return new SerialTactrixOpenPort(is, os);
                default:
                    throw new UnsupportedOperationException(communicationMode.name());
            }
        }
    }

}
