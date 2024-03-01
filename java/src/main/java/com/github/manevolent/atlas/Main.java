package com.github.manevolent.atlas;

import com.github.manevolent.atlas.can.*;
import com.github.manevolent.atlas.j2534.J2534Device;
import com.github.manevolent.atlas.j2534.J2534DeviceDescriptor;
import com.github.manevolent.atlas.j2534.serial.SerialTatrixOpenPortFactory;
import com.github.manevolent.atlas.j2534.tactrix.SerialTactrixOpenPort;
import com.github.manevolent.atlas.ssm4.Crypto;
import com.github.manevolent.atlas.subaru.SubaruDITComponent;
import com.github.manevolent.atlas.subaru.SubaruProtocols;
import com.github.manevolent.atlas.subaru.SubaruSecurityAccessCommand;
import com.github.manevolent.atlas.subaru.uds.request.SubaruStatus1Request;
import com.github.manevolent.atlas.uds.*;
import com.github.manevolent.atlas.uds.request.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static com.github.manevolent.atlas.subaru.SubaruDITComponent.*;

public class Main {

    private static void readDataById(AsyncUDSSession session, CANArbitrationId arbitrationId, int did) throws IOException {
        session.request(
                arbitrationId,
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
        AsyncUDSSession session = new AsyncUDSSession(device.openISOTOP(SubaruDITComponent.values()), protocol);
        session.start();

        session.request(
                ENGINE_2,
                new SubaruStatus1Request(0x0C),
                (response) -> {
                    System.out.println(response.toString());
                },
                Throwable::printStackTrace
        );

        System.out.println("Reading active diagnostic session...");
        readDataById(session, id(0x7A2), 0x11C8);
        readDataById(session, id(0x7A2), 0xF40C);
        readDataById(session, id(0x7A2), 0xF182);
        readDataById(session, id(0x752), 0xF186);
        readDataById(session, id(0x763), 0xF186);

        System.out.println("Entering diagnostic session with vehicle...");
        session.request(
                BROADCAST,
                new UDSDiagSessionControlRequest(DiagnosticSessionType.EXTENDED_SESSION),
                (response) -> {
                    System.out.println(response.toString());
                },
                Throwable::printStackTrace
        );

        Thread.sleep(250);

        System.out.println("Unlocking CGW...");
        new SubaruSecurityAccessCommand(0x7, UNKNOWN_1, Crypto.toByteArray("7692E7932F23A901568DDFA5FF580625"))
                .execute(session);

        System.out.println("Unlocking ECU...");
        new SubaruSecurityAccessCommand(0x1, ENGINE_1, Crypto.toByteArray("667E3078219976B4EDF3D43BD1D8FFC9"))
                .execute(session);

        System.out.println("Entering programming session...");
        session.request(
                ENGINE_1,
                new UDSDiagSessionControlRequest(DiagnosticSessionType.PROGRAMMING_SESSION),
                (response) -> {
                    System.out.println(response.toString());
                },
                Throwable::printStackTrace
        );

        System.out.println("Reading memory...");
        for (int offs = 0; offs < 1024; offs ++) {
            session.writer().write(BROADCAST, new UDSTesterPresentRequest());

            System.out.println("Reading memory by address...");
            session.request(
                    ENGINE_1,
                    new UDSReadMemoryByAddressRequest(1, 255, 4, 255),
                    (response) -> {
                        System.out.println(response.toString());
                    },
                    Throwable::printStackTrace
            );

        }
    }

}
