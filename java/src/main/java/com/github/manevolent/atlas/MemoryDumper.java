package com.github.manevolent.atlas;

import com.github.manevolent.atlas.protocol.j2534.J2534Device;
import com.github.manevolent.atlas.protocol.j2534.J2534DeviceDescriptor;
import com.github.manevolent.atlas.protocol.j2534.serial.SerialTatrixOpenPortFactory;
import com.github.manevolent.atlas.protocol.j2534.tactrix.SerialTactrixOpenPort;
import com.github.manevolent.atlas.protocol.uds.AsyncUDSSession;
import com.github.manevolent.atlas.protocol.uds.DiagnosticSessionType;
import com.github.manevolent.atlas.protocol.uds.RoutineControlSubFunction;
import com.github.manevolent.atlas.protocol.uds.UDSProtocol;
import com.github.manevolent.atlas.protocol.uds.request.*;
import com.github.manevolent.atlas.protocol.subaru.SubaruDITCommands;
import com.github.manevolent.atlas.protocol.subaru.SubaruProtocols;
import com.github.manevolent.atlas.protocol.subaru.uds.request.SubaruStatus1Request;


import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collection;

import static com.github.manevolent.atlas.protocol.subaru.SubaruDITComponent.*;

public class MemoryDumper {

    public static void reverse(byte[] array) {
        if (array == null) {
            return;
        }
        int i = 0;
        int j = array.length - 1;
        byte tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }

    private static void authorize(AsyncUDSSession session) throws IOException, InterruptedException {
        session.request(
                ENGINE_2,
                new SubaruStatus1Request(0x0C)
        );

        while (true) {
            try {
                //System.out.println("Entering diagnostic session with CGW...");
                session.request(
                        CENTRAL_GATEWAY,
                        new UDSDiagSessionControlRequest(DiagnosticSessionType.EXTENDED_SESSION)
                );

                //System.out.println("Unlocking CGW...");
                SubaruDITCommands.SECURITY_ACCESS_LEVEL_7.execute(session);

                //System.out.println("Starting unknown routine...");
                session.request(
                        CENTRAL_GATEWAY,
                        new UDSRoutineControlRequest(RoutineControlSubFunction.START_ROUTINE, 0x2, new byte[1])
                );

                //System.out.println("Entering extended session with ECU...");
                session.request(
                        ENGINE_1,
                        new UDSDiagSessionControlRequest(DiagnosticSessionType.EXTENDED_SESSION)
                );

                break;
            } catch (Exception ex) {
                ex.printStackTrace();
                Thread.sleep(1000L);
            }
        }
    }

    private static void checkRegion(AsyncUDSSession session, long begin) {
        fetchRegion(session, "0x" + Long.toHexString(begin) + ".bin", begin, begin + 0x1);
    }

    private static void fetchRegion(AsyncUDSSession session, String filename, long begin, long end) {
        int maxReadSize = 0x10;

        int didoffs = 0;
        try (RandomAccessFile raf = new RandomAccessFile(filename, "rw")) {
            for (long offset = begin; offset < end; offset += maxReadSize, didoffs++) {

                if(didoffs >= 20) {
                    didoffs = 0;
                }

                if (didoffs == 0) {
                    System.out.println(filename + ": " + Math.round(((double)offset / (double)end) * 100000D) / 1000d
                            + "%...");

                    session.request(
                            ENGINE_1,
                            new UDSDiagSessionControlRequest(DiagnosticSessionType.DEFAULT_SESSION)
                    );

                    authorize(session);

                    SubaruDITCommands.SECURITY_ACCESS_LEVEL_3.execute(session);
                }

                session.request(
                        ENGINE_1,
                        new UDSDiagSessionControlRequest(DiagnosticSessionType.EXTENDED_SESSION)
                );

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                BitWriter bitWriter = new BitWriter(baos);
                bitWriter.write(0x14);
                bitWriter.writeInt((int) (offset & 0xFFFFFFFFL));
                bitWriter.write((byte) Math.min(maxReadSize, end - offset));

                try {
                    session.request(ENGINE_1, new UDSDefineDataIdentifierRequest(2, 0xF300 + didoffs, baos.toByteArray()));
                } catch (IOException ex) {
                    ex.printStackTrace();
                    System.out.println("Out of range: " + offset);
                    continue;
                }

                try {
                    long finalOffset = offset;

                    session.request(ENGINE_1, new UDSReadDataByIDRequest(0xF300 + didoffs), (response) -> {
                        try {
                            raf.seek(finalOffset - begin);
                            byte[] data = response.getData();
                            reverse(data);
                            raf.write(response.getData());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } catch (Exception ex) {
                    throw ex;
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        SerialTatrixOpenPortFactory canDeviceFactory =
                new SerialTatrixOpenPortFactory(SerialTactrixOpenPort.CommunicationMode.DIRECT_SOCKET);

        Collection<J2534DeviceDescriptor> devices = canDeviceFactory.findDevices();
        J2534DeviceDescriptor deviceDescriptor = devices.stream().findFirst().orElseThrow(() ->
                new IllegalArgumentException("No can devices found"));
        J2534Device device = deviceDescriptor.createDevice();
        UDSProtocol protocol = SubaruProtocols.DIT;
        AsyncUDSSession session = new AsyncUDSSession(device.openISOTOP(
                ENGINE_1,
                ENGINE_2,
                BODY_CONTROL,
                CENTRAL_GATEWAY
        ), protocol);
        session.start();

        for (int i = 0x10000 - 32; i <= 0x10000 + 100; i += 1) {
            long regionStart = i;
            long regionEnd = regionStart + 1;
            System.out.println("Read " + Long.toHexString(regionStart));
            fetchRegion(session, "test.bin", regionStart, regionEnd);
        }
    }

}
