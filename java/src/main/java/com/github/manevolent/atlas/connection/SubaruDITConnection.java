package com.github.manevolent.atlas.connection;

import com.github.manevolent.atlas.model.Rom;
import com.github.manevolent.atlas.model.RomProperty;
import com.github.manevolent.atlas.model.uds.SecurityAccessProperty;
import com.github.manevolent.atlas.protocol.j2534.ISOTPDevice;
import com.github.manevolent.atlas.protocol.j2534.J2534Device;
import com.github.manevolent.atlas.protocol.j2534.J2534DeviceDescriptor;
import com.github.manevolent.atlas.protocol.j2534.serial.SerialTatrixOpenPortFactory;
import com.github.manevolent.atlas.protocol.j2534.tactrix.SerialTactrixOpenPort;
import com.github.manevolent.atlas.protocol.subaru.SubaruDITCommands;
import com.github.manevolent.atlas.protocol.subaru.SubaruProtocols;
import com.github.manevolent.atlas.protocol.subaru.SubaruSecurityAccessCommandAES;
import com.github.manevolent.atlas.protocol.subaru.uds.request.SubaruStatus1Request;
import com.github.manevolent.atlas.protocol.uds.*;
import com.github.manevolent.atlas.protocol.uds.command.UDSSecurityAccessCommand;
import com.github.manevolent.atlas.protocol.uds.request.*;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.TimeoutException;

import static com.github.manevolent.atlas.protocol.subaru.SubaruDITComponent.*;
import static com.github.manevolent.atlas.protocol.subaru.SubaruDITComponent.CENTRAL_GATEWAY;

public class SubaruDITConnection extends UDSConnection {
    private final Rom rom;

    public SubaruDITConnection(Rom rom) {
        this.rom = rom;
    }

    @Override
    protected UDSComponent getECUComponent() {
        return ENGINE_1;
    }

    @Override
    protected UDSSession connect() throws IOException {
        SerialTatrixOpenPortFactory canDeviceFactory =
                new SerialTatrixOpenPortFactory(SerialTactrixOpenPort.CommunicationMode.DIRECT_SOCKET);
        Collection<J2534DeviceDescriptor> devices = canDeviceFactory.findDevices();
        J2534DeviceDescriptor deviceDescriptor = devices.stream().findFirst().orElseThrow(() ->
                new IllegalArgumentException("No can devices found"));
        J2534Device device = deviceDescriptor.createDevice();
        UDSProtocol protocol = SubaruProtocols.DIT;
        ISOTPDevice isotpDevice = device.openISOTOP(
                ENGINE_1,
                ENGINE_2,
                BODY_CONTROL,
                CENTRAL_GATEWAY
        );
        AsyncUDSSession session = new AsyncUDSSession(isotpDevice, protocol);
        session.start();
        return session;
    }

    @Override
    protected void change(ConnectionMode newMode) throws IOException, TimeoutException {
        // Select AES key for the gateway
        String propertyFormat = "subaru.dit.aeskey.%s";
        SecurityAccessProperty cgwAccessProperty;
        cgwAccessProperty = rom.getProperty(String.format(propertyFormat, "gateway"), SecurityAccessProperty.class);

        // Set AES key for the ECU
        String propertyName;
        switch (newMode) {
            case READ_MEMORY -> propertyName = String.format(propertyFormat, "memory_read");
            case WRITE_MEMORY -> propertyName = String.format(propertyFormat, "memory_write");
            case FLASH_ROM -> propertyName = String.format(propertyFormat, "flash_write");
            default -> throw new UnsupportedOperationException(newMode.name());
        }
        SecurityAccessProperty engineAccessProperty;
        engineAccessProperty = rom.getProperty(propertyName, SecurityAccessProperty.class);
        if (engineAccessProperty == null) {
            throw new IllegalArgumentException("Missing security access property for " + newMode);
        }

        AsyncUDSSession session = (AsyncUDSSession) getSession();

        // Ping the system to ensure we are connected
        session.request(
                ENGINE_2,
                new SubaruStatus1Request(0x0C)
        );

        // Change the ECU into a default session
        session.request(
                ENGINE_1,
                new UDSDiagSessionControlRequest(DiagnosticSessionType.DEFAULT_SESSION)
        );

        // Optionally authorize with the CGW if a key is present
        if (cgwAccessProperty != null) {
            session.request(
                    CENTRAL_GATEWAY,
                    new UDSDiagSessionControlRequest(DiagnosticSessionType.EXTENDED_SESSION)
            );

            // Use the CGW AES key
            new SubaruSecurityAccessCommandAES(
                    cgwAccessProperty.getLevel(),
                    CENTRAL_GATEWAY,
                    cgwAccessProperty.getKey()
            ).execute(session);

            // Instruct CGW to allow communication to ECU
            session.request(
                    CENTRAL_GATEWAY,
                    new UDSRoutineControlRequest(RoutineControlSubFunction.START_ROUTINE, 0x2, new byte[]{0x00})
            );
        }

        // Enter an extended session with the ECU
        session.request(
                ENGINE_1,
                new UDSDiagSessionControlRequest(DiagnosticSessionType.EXTENDED_SESSION)
        );

        // Use the engine AES key
        new SubaruSecurityAccessCommandAES(
                engineAccessProperty.getLevel(),
                ENGINE_1,
                engineAccessProperty.getKey()
        ).execute(session);

        // If we need to enter a programming session...
        if (newMode == ConnectionMode.FLASH_ROM) {
            session.request(
                    ENGINE_1,
                    new UDSDiagSessionControlRequest(DiagnosticSessionType.PROGRAMMING_SESSION)
            );
        }
    }

    @Override
    protected UDSProtocol getProtocol() {
        return SubaruProtocols.DIT;
    }

}
