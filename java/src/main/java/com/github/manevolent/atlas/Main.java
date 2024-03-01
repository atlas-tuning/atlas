package com.github.manevolent.atlas;

import com.github.manevolent.atlas.j2534.J2534Device;
import com.github.manevolent.atlas.j2534.J2534DeviceDescriptor;
import com.github.manevolent.atlas.j2534.serial.SerialTatrixOpenPortFactory;
import com.github.manevolent.atlas.j2534.tactrix.SerialTactrixOpenPort;
import com.github.manevolent.atlas.ssm4.Crypto;
import com.github.manevolent.atlas.subaru.SubaruDITCommands;
import com.github.manevolent.atlas.subaru.SubaruProtocols;
import com.github.manevolent.atlas.subaru.SubaruSecurityAccessCommandAES;
import com.github.manevolent.atlas.subaru.uds.request.SubaruStatus1Request;
import com.github.manevolent.atlas.uds.*;
import com.github.manevolent.atlas.uds.request.*;

import java.io.IOException;
import java.util.Collection;

import static com.github.manevolent.atlas.subaru.SubaruDITComponent.*;

public class Main {

    private static void readDataById(AsyncUDSSession session, UDSComponent component, int did) throws IOException {
        session.request(
                component.getSendAddress(),
                new UDSReadDataByIDRequest(did),
                (response) -> {},
                Throwable::printStackTrace
        );
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

        session.request(
                ENGINE_2,
                new SubaruStatus1Request(0x0C)
        );


        while (true) {
            try {
                System.out.println("Reading active diagnostic session...");
                readDataById(session, ENGINE_1, 0x11C8);
                readDataById(session, ENGINE_1, 0xF40C);
                readDataById(session, ENGINE_1, 0xF182);
                readDataById(session, BODY_CONTROL, 0xF186);
                readDataById(session, CENTRAL_GATEWAY, 0xF186);

                System.out.println("Entering diagnostic session with vehicle...");
                session.request(
                        BROADCAST,
                        new UDSDiagSessionControlRequest(DiagnosticSessionType.EXTENDED_SESSION)
                );

                Thread.sleep(250);

                System.out.println("Unlocking CGW...");
                SubaruDITCommands.SECURITY_ACCESS_LEVEL_7.execute(session);

                System.out.println("Starting routine...");
                session.request(
                        CENTRAL_GATEWAY,
                        new UDSRoutineControlRequest(RoutineControlSubFunction.START_ROUTINE, 0x2, new byte[1])
                );

                session.writer().write(BROADCAST, new UDSTesterPresentRequest((byte) 0x80));

                // Communication control
                try {
                    session.request(BROADCAST, new UDSCommunicationControlRequest(2));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                Thread.sleep(250);

                session.writer().write(BROADCAST, new UDSTesterPresentRequest((byte) 0x80));
                session.writer().write(BROADCAST, new UDSCommunicationControlRequest(3, new byte[]{0x1}));

                Thread.sleep(250);

                // Starting routine
                System.out.println("Starting routine...");
                session.request(
                        CENTRAL_GATEWAY,
                        new UDSRoutineControlRequest(RoutineControlSubFunction.START_ROUTINE, 0x2, new byte[]{0x1})
                );

                SubaruDITCommands.SECURITY_ACCESS_LEVEL_1.execute(session);
                break;
            } catch (Exception ex) {
                ex.printStackTrace();
                Thread.sleep(1000L);
            }
        }

        session.request(
                ENGINE_1,
                new UDSDiagSessionControlRequest(DiagnosticSessionType.EXTENDED_SESSION)
        );

        System.out.println("Reading memory...");
        session.request(ENGINE_1, new UDSReadMemoryByAddressRequest(4, 0x0FFF, 1, 16));
    }

}
