package com.github.manevolent.atlas;

import com.github.manevolent.atlas.j2534.J2534Device;
import com.github.manevolent.atlas.j2534.J2534DeviceDescriptor;
import com.github.manevolent.atlas.j2534.serial.SerialTatrixOpenPortFactory;
import com.github.manevolent.atlas.j2534.tactrix.SerialTactrixOpenPort;
import com.github.manevolent.atlas.ssm4.Crypto;
import com.github.manevolent.atlas.subaru.SubaruDITCommands;
import com.github.manevolent.atlas.subaru.SubaruDITReadMemoryCommand;
import com.github.manevolent.atlas.subaru.SubaruProtocols;
import com.github.manevolent.atlas.subaru.SubaruSecurityAccessCommandAES;
import com.github.manevolent.atlas.subaru.uds.request.SubaruStatus1Request;
import com.github.manevolent.atlas.uds.*;
import com.github.manevolent.atlas.uds.request.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import static com.github.manevolent.atlas.subaru.SubaruDITComponent.*;

public class Main {

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
                System.out.println("Entering diagnostic session with CGW...");
                session.request(
                        CENTRAL_GATEWAY,
                        new UDSDiagSessionControlRequest(DiagnosticSessionType.EXTENDED_SESSION)
                );

                System.out.println("Unlocking CGW...");
                SubaruDITCommands.SECURITY_ACCESS_LEVEL_7.execute(session);

                System.out.println("Starting unknown routine...");
                session.request(
                        CENTRAL_GATEWAY,
                        new UDSRoutineControlRequest(RoutineControlSubFunction.START_ROUTINE, 0x2, new byte[1])
                );

                System.out.println("Entering extended session with ECU...");
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

        int maxReadSize = 0x32;

        try (RandomAccessFile raf = new RandomAccessFile("memory.bin", "rw")) {
            for (long offset = 0x000B1102; offset < 0xFFFFFFFFL; offset += maxReadSize) {
                authorize(session);

                SubaruDITCommands.SECURITY_ACCESS_LEVEL_3.execute(session);

                System.out.println("Entering extended session with ECU...");
                session.request(
                        ENGINE_1,
                        new UDSDiagSessionControlRequest(DiagnosticSessionType.EXTENDED_SESSION)
                );

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                BitWriter bitWriter = new BitWriter(baos);
                bitWriter.write(0x14);
                bitWriter.writeInt((int) (offset & 0xFFFFFFFF));
                bitWriter.write(maxReadSize);

                try {
                    session.request(ENGINE_1, new UDSDefineDataIdentifierRequest(2, 0xF300, baos.toByteArray()));
                    long finalOffset = offset;

                    session.request(ENGINE_1, new UDSReadDataByIDRequest(0xF300), (response) -> {
                        try {
                            raf.seek(finalOffset);
                            byte[] data = response.getData();
                            reverse(data);
                            raf.write(response.getData());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } catch (Exception ex) {
                    System.exit(-1);
                }

                System.out.println("Leaving extended session with ECU...");
                session.request(
                        ENGINE_1,
                        new UDSDiagSessionControlRequest(DiagnosticSessionType.DEFAULT_SESSION)
                );
            }
        }
    }

}
