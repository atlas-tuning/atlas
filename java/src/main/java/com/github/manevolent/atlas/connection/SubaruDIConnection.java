package com.github.manevolent.atlas.connection;

import com.github.manevolent.atlas.BitWriter;
import com.github.manevolent.atlas.model.MemoryAddress;
import com.github.manevolent.atlas.model.MemoryParameter;
import com.github.manevolent.atlas.model.Project;
import com.github.manevolent.atlas.model.uds.SecurityAccessProperty;
import com.github.manevolent.atlas.protocol.isotp.ISOTPFrameReader;
import com.github.manevolent.atlas.protocol.isotp.ISOTPSpyDevice;
import com.github.manevolent.atlas.protocol.j2534.ISOTPDevice;
import com.github.manevolent.atlas.protocol.j2534.J2534Device;
import com.github.manevolent.atlas.protocol.j2534.J2534DeviceProvider;
import com.github.manevolent.atlas.protocol.subaru.SubaruProtocols;
import com.github.manevolent.atlas.protocol.subaru.SubaruSecurityAccessCommandAES;
import com.github.manevolent.atlas.protocol.subaru.uds.request.SubaruStatus1Request;
import com.github.manevolent.atlas.protocol.uds.*;
import com.github.manevolent.atlas.protocol.uds.request.*;
import com.github.manevolent.atlas.protocol.uds.response.UDSReadDataByIDResponse;
import com.github.manevolent.atlas.protocol.uds.response.UDSReadMemoryByAddressResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.TimeoutException;

import static com.github.manevolent.atlas.protocol.subaru.SubaruDITComponent.*;
import static com.github.manevolent.atlas.protocol.subaru.SubaruDITComponent.CENTRAL_GATEWAY;

public class SubaruDIConnection extends UDSConnection {
    private static final String securityAccessPropertyFormat = "subaru.dit.securityaccess.%s";
    private static final String gatewayKeyProperty = String.format(securityAccessPropertyFormat, "gateway");
    private static final String memoryReadKeyProperty = String.format(securityAccessPropertyFormat, "memory_read");
    private static final String memoryWriteKeyProperty = String.format(securityAccessPropertyFormat, "memory_write");
    private static final String flashWriteKeyProperty = String.format(securityAccessPropertyFormat, "flash_write");
    private static final String datalogKeyProperty = String.format(securityAccessPropertyFormat, "datalog");

    private static final UDSProtocol protocol = SubaruProtocols.DIT;

    /**
     * ECU orders things in non-native order
     * @param array array to reverse
     */
    private static void reverse(byte[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            byte temp = array[i];
            array[i] = array[array.length - 1 - i];
            array[array.length - 1 - i] = temp;
        }
    }

    private static final int DEFAULT_DID = 0xF300;
    private final Set<MemoryParameter> activeParameters = new LinkedHashSet<>();

    private final int did;

    public SubaruDIConnection(J2534DeviceProvider provider) {
        super(provider);
        did = DEFAULT_DID;
    }

    @Override
    protected UDSComponent getECUComponent() {
        return ENGINE_1;
    }

    @Override
    public UDSSession connect() throws IOException, TimeoutException {
        changeConnectionMode(ConnectionMode.DISCONNECTED);

        J2534Device device = findDevice();
        if (device == null) {
            throw new NullPointerException("No J2534 device found");
        }

        ISOTPDevice isotpDevice = device.openISOTOP(ENGINE_1, ENGINE_2, BODY_CONTROL, CENTRAL_GATEWAY);
        AsyncUDSSession session = new AsyncUDSSession(isotpDevice, protocol);
        session.start();

        setConnectionMode(ConnectionMode.IDLE);
        return session;
    }

    @Override
    public UDSSession spy() throws IOException, TimeoutException {
        changeConnectionMode(ConnectionMode.DISCONNECTED);

        J2534Device device = findDevice();
        ISOTPFrameReader isotpReader = new ISOTPFrameReader(device.openCAN().reader());
        ISOTPSpyDevice spyDevice = new ISOTPSpyDevice(isotpReader);
        AsyncUDSSession session = new AsyncUDSSession(spyDevice, protocol);
        setSession(session);
        session.start();

        setConnectionMode(ConnectionMode.IDLE);
        return session;
    }

    @Override
    public int getMaximumReadSize() {
        return 0x32;
    }

    @Override
    public byte[] readMemory(MemoryAddress address, int length) throws IOException, TimeoutException {
        long offset = address.getOffset();
        int maxReadSize = getMaximumReadSize();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitWriter bitWriter = new BitWriter(baos);
        bitWriter.write(0x14);
        bitWriter.writeInt((int) (offset & 0xFFFFFFFF));
        bitWriter.write((byte) Math.min(0xFF, length));

        AsyncUDSSession session = (AsyncUDSSession) getSession();
        try (UDSTransaction<UDSReadMemoryByAddressRequest, UDSReadMemoryByAddressResponse>
                     transaction = session.request(getECUComponent().getSendAddress(),
                new UDSReadMemoryByAddressRequest(4, offset, 1, length))) {
            byte[] data = transaction.get().getData();
            reverse(data);
            return data;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    protected void change(ConnectionMode newMode) throws IOException, TimeoutException {
        if (newMode == ConnectionMode.DISCONNECTED) {
            setSession(null);
            return;
        }

        Project project = getProject();
        if (project == null) {
            throw new IllegalStateException("Project is not set");
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

        if (newMode == ConnectionMode.IDLE) {
            // If we're idle, exit now
            return;
        }

        // Select AES key for the gateway
        SecurityAccessProperty cgwAccessProperty;
        cgwAccessProperty = project.getProperty(gatewayKeyProperty, SecurityAccessProperty.class);

        // Set AES key for the ECU
        String propertyName;
        switch (newMode) {
            case READ_MEMORY -> propertyName = memoryReadKeyProperty;
            case WRITE_MEMORY -> propertyName = memoryWriteKeyProperty;
            case FLASH_ROM -> propertyName = flashWriteKeyProperty;
            case DATALOG -> propertyName = datalogKeyProperty;
            default -> throw new UnsupportedOperationException(newMode.name());
        }
        SecurityAccessProperty engineAccessProperty;
        engineAccessProperty = project.getProperty(propertyName, SecurityAccessProperty.class);
        if (engineAccessProperty == null) {
            throw new IllegalArgumentException("Missing security access property for " + newMode);
        }

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

        keepAlive();

        // If we need to enter a programming session...
        if (newMode == ConnectionMode.FLASH_ROM) {
            session.request(
                    ENGINE_1,
                    new UDSDiagSessionControlRequest(DiagnosticSessionType.PROGRAMMING_SESSION)
            );

            // TODO wait for bootloader mode
        }
    }

    @Override
    protected void keepAlive() throws IOException, TimeoutException {
        if (getConnectionMode() == ConnectionMode.IDLE) {
            // This doesn't need tester present
            return;
        }

        getSession().request(BROADCAST.getSendAddress(), new UDSTesterPresentRequest(new byte[] { (byte) 0x80 }));
    }

    @Override
    protected UDSProtocol getProtocol() {
        return SubaruProtocols.DIT;
    }


    @Override
    public MemoryFrame readFrame(Collection<MemoryParameter> parameters) {
        if (parameters.isEmpty()) {
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

        try (UDSTransaction<UDSReadDataByIDRequest, UDSReadDataByIDResponse> transaction =
                     getSession().request(ENGINE_1.getSendAddress(), new UDSReadDataByIDRequest(did))) {
            UDSReadDataByIDResponse response = transaction.get();

            MemoryFrame frame = new MemoryFrame();

            ByteArrayInputStream bais = new ByteArrayInputStream(response.getData());

            for (MemoryParameter parameter : activeParameters) {
                byte[] data = parameter.newBuffer();
                if (bais.read(data) != data.length) {
                    throw new EOFException("Unexpected end of data");
                }
                reverse(data);
                frame.setData(parameter, data);
            }

            return frame;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class Factory implements ConnectionFactory {
        @Override
        public Connection createConnection(J2534DeviceProvider deviceProvider) {
            return new SubaruDIConnection(deviceProvider);
        }

        @Override
        public List<ConnectionParameter> getParameters() {
            return Arrays.asList(
                    new ConnectionParameter(true, gatewayKeyProperty,
                            "Gateway Access Key",
                            "The security access configuration for disarming the central gateway module",
                            SecurityAccessProperty.class),
                    new ConnectionParameter(true, memoryReadKeyProperty,
                            "Memory Read Key",
                            "The security access configuration for placing the ECU into memory read mode",
                            SecurityAccessProperty.class),
                    new ConnectionParameter(true, memoryWriteKeyProperty,
                            "Memory Write Key",
                            "The security access configuration for placing the ECU into memory write mode",
                            SecurityAccessProperty.class),
                    new ConnectionParameter(true, flashWriteKeyProperty,
                            "Flash Write Key",
                            "The security access configuration for placing the ECU into programming mode",
                            SecurityAccessProperty.class),
                    new ConnectionParameter(true, datalogKeyProperty,
                            "Datalog Key",
                            "The security access configuration for placing the ECU into a datalog session",
                            SecurityAccessProperty.class)
            );
        }
    }
}
