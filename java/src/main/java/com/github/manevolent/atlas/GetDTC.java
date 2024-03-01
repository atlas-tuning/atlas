package com.github.manevolent.atlas;

import com.github.manevolent.atlas.j2534.J2534Device;
import com.github.manevolent.atlas.j2534.J2534DeviceDescriptor;
import com.github.manevolent.atlas.j2534.serial.SerialTatrixOpenPortFactory;
import com.github.manevolent.atlas.j2534.tactrix.SerialTactrixOpenPort;
import com.github.manevolent.atlas.subaru.SubaruDITCommands;
import com.github.manevolent.atlas.subaru.SubaruProtocols;
import com.github.manevolent.atlas.subaru.uds.request.SubaruReadDTCRequest;
import com.github.manevolent.atlas.subaru.uds.response.SubaruReadDTCResponse;
import com.github.manevolent.atlas.uds.AsyncUDSSession;
import com.github.manevolent.atlas.uds.DiagnosticSessionType;
import com.github.manevolent.atlas.uds.RoutineControlSubFunction;
import com.github.manevolent.atlas.uds.UDSProtocol;
import com.github.manevolent.atlas.uds.request.UDSDiagSessionControlRequest;
import com.github.manevolent.atlas.uds.request.UDSReadDTCRequest;
import com.github.manevolent.atlas.uds.request.UDSRoutineControlRequest;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import static com.github.manevolent.atlas.subaru.SubaruDITComponent.*;
import static com.github.manevolent.atlas.subaru.SubaruDITComponent.CENTRAL_GATEWAY;

public class GetDTC {

    public static void main(String[] args) throws IOException {
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

        Set<Short> response = SubaruDITCommands.READ_DTC.execute(session);
        System.out.println(response);
    }

}