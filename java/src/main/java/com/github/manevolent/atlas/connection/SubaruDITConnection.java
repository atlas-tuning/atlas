package com.github.manevolent.atlas.connection;

import com.github.manevolent.atlas.model.MemoryParameter;
import com.github.manevolent.atlas.model.Project;
import com.github.manevolent.atlas.model.uds.SecurityAccessProperty;
import com.github.manevolent.atlas.protocol.j2534.ISOTPDevice;
import com.github.manevolent.atlas.protocol.j2534.J2534Device;
import com.github.manevolent.atlas.protocol.j2534.J2534DeviceDescriptor;
import com.github.manevolent.atlas.protocol.j2534.serial.SerialTatrixOpenPortFactory;
import com.github.manevolent.atlas.protocol.j2534.tactrix.SerialTactrixOpenPort;
import com.github.manevolent.atlas.protocol.subaru.SubaruProtocols;
import com.github.manevolent.atlas.protocol.subaru.SubaruSecurityAccessCommandAES;
import com.github.manevolent.atlas.protocol.subaru.uds.request.SubaruStatus1Request;
import com.github.manevolent.atlas.protocol.uds.*;
import com.github.manevolent.atlas.protocol.uds.request.*;
import com.github.manevolent.atlas.protocol.uds.response.UDSReadDataByIDResponse;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import static com.github.manevolent.atlas.protocol.subaru.SubaruDITComponent.*;
import static com.github.manevolent.atlas.protocol.subaru.SubaruDITComponent.CENTRAL_GATEWAY;

public class SubaruDITConnection extends UDSConnection {
    private static final int DEFAULT_DID = 0xF300;
    private final Set<MemoryParameter> activeParameters = new LinkedHashSet<>();
    private final Project project;

    private final int did;

    public SubaruDITConnection(Project project) {
        this.project = project;

        if (project.hasProperty("subaru.dit.datalog.did")) {
            //TODO custom dids
        }

        did = DEFAULT_DID;
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
        String propertyFormat = "subaru.dit.securityaccess.%s";
        SecurityAccessProperty cgwAccessProperty;
        cgwAccessProperty = project.getProperty(String.format(propertyFormat, "gateway"), SecurityAccessProperty.class);

        // Set AES key for the ECU
        String propertyName;
        switch (newMode) {
            case READ_MEMORY -> propertyName = String.format(propertyFormat, "memory_read");
            case WRITE_MEMORY -> propertyName = String.format(propertyFormat, "memory_write");
            case FLASH_ROM -> propertyName = String.format(propertyFormat, "flash_write");
            default -> throw new UnsupportedOperationException(newMode.name());
        }
        SecurityAccessProperty engineAccessProperty;
        engineAccessProperty = project.getProperty(propertyName, SecurityAccessProperty.class);
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

        session.request(ENGINE_1.getSendAddress(), new UDSTesterPresentRequest(new byte[] { (byte) 0x80 }));

        // If we need to enter a programming session...
        if (newMode == ConnectionMode.FLASH_ROM) {
            session.request(
                    ENGINE_1,
                    new UDSDiagSessionControlRequest(DiagnosticSessionType.PROGRAMMING_SESSION)
            );
        }
    }

    @Override
    protected void keepAlive() throws IOException, TimeoutException {
        //getSession().request(BROADCAST.getSendAddress(), new UDSTesterPresentRequest(new byte[] { (byte) 0x80 }));

    }

    @Override
    protected UDSProtocol getProtocol() {
        return SubaruProtocols.DIT;
    }

    private void invert(byte[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            byte temp = array[i];
            array[i] = array[array.length - 1 - i];
            array[array.length - 1 - i] = temp;
        }
    }

    @Override
    public MemoryFrame readFrame(Collection<MemoryParameter> parameters) {
        if (parameters.isEmpty()) {
            try {
                getSession().request(ENGINE_1.getSendAddress(), new UDSTesterPresentRequest(new byte[] { (byte) 0x80 }));
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }

            return null;
        }

        boolean changed = parameters.size() != activeParameters.size() ||
                parameters.stream().anyMatch(x -> !activeParameters.contains(x)) ||
                activeParameters.stream().anyMatch(x -> !parameters.contains(x));

        if (changed) {
            // Delete any prior DID
            try (var transaction = getSession().request(ENGINE_1.getSendAddress(),
                    new UDSDefineDataIdentifierRequest(0x3, did))) {
                // Do nothing
                transaction.get();
            } catch (Exception e) {
                // Ignore
                e.printStackTrace();
            }

            ByteBuffer buffer = ByteBuffer.allocate(1 + (parameters.size() * 5));

            // Length and data length
            // 0x1 - data length, length (i.e. 0xFF = 255 bytes at address)
            // 0x4 - address length (i.e. 32 bit memory address / 0xFFFFFFFF)
            buffer.put((byte) 0x14);

            // Watch out - the order switches here from data length and memory to vice versa:
            for (MemoryParameter parameter : parameters) {
                buffer.putInt((int) (parameter.getAddress().getOffset() & 0xFFFFFFFFL));
                buffer.put((byte) (parameter.getScale().getFormat().getSize() & 0xFF));
            }

            // Set up new DID
            try (var transaction = getSession().request(ENGINE_1.getSendAddress(), new UDSDefineDataIdentifierRequest(
                    0x2, did, buffer.array()))) {
                // Do nothing
                transaction.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // Ensure the order is preserved
            this.activeParameters.clear();
            this.activeParameters.addAll(new LinkedHashSet<>(parameters));
        }

        try (UDSTransaction<UDSReadDataByIDResponse> transaction =
                     getSession().request(ENGINE_1.getSendAddress(), new UDSReadDataByIDRequest(did))) {
            UDSReadDataByIDResponse response = transaction.get();

            MemoryFrame frame = new MemoryFrame();

            ByteArrayInputStream bais = new ByteArrayInputStream(response.getData());

            for (MemoryParameter parameter : activeParameters) {
                byte[] data = new byte[parameter.getScale().getFormat().getSize()];
                if (bais.read(data) != data.length) {
                    throw new EOFException("Unexpected end of data");
                }
                invert(data);
                frame.setData(parameter, data);
            }

            return frame;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
