package com.github.manevolent.atlas.connection;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.logging.Log;
import com.github.manevolent.atlas.model.MemoryParameter;
import com.github.manevolent.atlas.model.Project;
import com.github.manevolent.atlas.protocol.j2534.J2534Device;
import com.github.manevolent.atlas.protocol.j2534.J2534DeviceDescriptor;
import com.github.manevolent.atlas.protocol.j2534.J2534DeviceProvider;
import com.github.manevolent.atlas.protocol.uds.*;
import com.github.manevolent.atlas.protocol.uds.request.UDSClearDTCInformationRequest;
import com.github.manevolent.atlas.protocol.uds.request.UDSECUResetRequest;
import com.github.manevolent.atlas.protocol.uds.request.UDSReadDTCRequest;
import com.github.manevolent.atlas.protocol.uds.request.UDSReadDataByIDRequest;
import com.github.manevolent.atlas.protocol.uds.response.UDSReadDTCResponse;
import com.github.manevolent.atlas.ui.util.Devices;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.logging.Level;

public abstract class UDSConnection implements Connection, UDSListener {
    private final J2534DeviceProvider deviceProvider;
    private ConnectionMode connectionMode = ConnectionMode.DISCONNECTED;
    private Set<MemoryParameter> parameters = new LinkedHashSet<>();
    private LinkedList<Consumer<MemoryFrame>> listeners = new LinkedList<>();
    private UDSSession session;
    private KeepAliveThread keepAliveThread ;
    private final Object stateObject = new Object();
    private long lastFrameSent = 0L;
    private Project project;

    public UDSConnection(J2534DeviceProvider deviceProvider) {
        this.deviceProvider = deviceProvider;
    }

    @Override
    public boolean isConnected() {
        return session != null && getConnectionMode() != ConnectionMode.DISCONNECTED;
    }

    @Override
    public ConnectionMode getConnectionMode() {
        return connectionMode;
    }

    protected J2534Device findDevice() throws IOException {
        J2534DeviceProvider provider = Devices.getProvider();
        J2534DeviceDescriptor defaultDevice = provider.getDefaultDevice();
        if (defaultDevice == null) {
            throw new NullPointerException("No default J2534 device found");
        }

        return defaultDevice.createDevice();
    }

    protected void onProjectChanging(Project newProject) {

    }

    public void setProject(Project project) {
        if (this.project != project) {
            onProjectChanging(project);
            this.project = project;
        }
    }

    public Project getProject() {
        return project;
    }

    /**
     * Gets the interval between keep-alive events, and the delay after any frame is sent
     * where a keep-alive will first be sent.
     *
     * This is intended to operate with "tester present" frames in UDS.
     *
     * @return milliseconds.
     */
    public long getKeepAliveInterval() {
        return 1000L;
    }

    /**
     * Sends a keep-alive message (i.e. tester present) to the receiving endpoint
     * @throws IOException
     * @throws TimeoutException
     */
    protected abstract void keepAlive() throws IOException, TimeoutException;

    /**
     * Gets the current UDS session
     * @return UDS session
     */
    public UDSSession getSession() throws IOException, TimeoutException {
        if (session == null) {
            session = connect();
            session.addListener(this);
            changeConnectionMode(ConnectionMode.IDLE);
        }

        return session;
    }

    /**
     * Internally sets the connection mode, but does not fire connection events.
     * @param mode new mode.
     */
    protected void setConnectionMode(ConnectionMode mode) {
        this.connectionMode = mode;
    }

    protected abstract UDSProtocol getProtocol();

    protected abstract UDSComponent getECUComponent();

    protected void onMemoryFrame(MemoryFrame frame) {
        listeners.forEach(listener -> listener.accept(frame));
    }

    @Override
    public void changeConnectionMode(ConnectionMode newMode) throws IOException, TimeoutException {
        if (newMode == connectionMode) {
            return;
        }

        synchronized (stateObject) {
            if (connectionMode == ConnectionMode.DISCONNECTED && session == null) {
                stateObject.notifyAll();
                session = connect();
            } else if (newMode == ConnectionMode.DISCONNECTED) {
                if (session != null) {
                    session.close();
                    session = null;
                }
                connectionMode = ConnectionMode.DISCONNECTED;
                stateObject.notifyAll();
                return;
            }

            change(newMode);
            connectionMode = newMode;

            if (keepAliveThread == null || !keepAliveThread.isAlive()) {
                keepAliveThread = new KeepAliveThread();
                keepAliveThread.setName("UDS Keep Alive");
                keepAliveThread.setDaemon(true);
                keepAliveThread.start();
            }
        }
    }

    protected void setSession(UDSSession session) throws IOException {
        if (this.session != null) {
            this.session.close();
        }

        this.session = session;
    }

    public abstract UDSSession connect() throws IOException, TimeoutException;

    @Override
    public boolean isSpying() {
        return session != null && session.isSpying();
    }

    @Override
    public void disconnect() throws IOException, TimeoutException {
        changeConnectionMode(ConnectionMode.DISCONNECTED);
    }

    protected abstract void change(ConnectionMode newMode) throws IOException, TimeoutException;

    @Override
    public void onUDSFrameWrite(UDSFrame frame) {
        lastFrameSent = System.currentTimeMillis();
    }

    @Override
    public void onUDSFrameRead(UDSFrame frame) {
        // Do nothing
    }

    @Override
    public void clearDTC() throws IOException, TimeoutException {
        try {
            try (var transaction = getSession().request(getECUComponent().getSendAddress(),
                    new UDSClearDTCInformationRequest(new byte[] {
                            (byte)0xFF, (byte)0xFF, (byte)0xFF
                    }))) {
                transaction.join();
            }
        } catch (IOException | TimeoutException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public List<Integer> readDTC() throws IOException, TimeoutException {
        try {
            try (var transaction = getSession().request(getECUComponent().getSendAddress(),
                    new UDSReadDTCRequest(DTC.REPORT_DTC_BY_MASK, DTC.MASK_CONFIRMED))) {
                UDSReadDTCResponse response = transaction.get();

                List<Integer> dtc = new ArrayList<>();;
                BitReader reader = new BitReader(response.getData());
                reader.readByte(); // Skip report type
                reader.readByte(); // Skip availability mask

                while (reader.remainingBytes() >= 4) {
                    dtc.add((int) (reader.read(24) & 0xFFFFFF));
                    reader.readByte(); // Skip the status bit
                }
                return dtc;

            }
        } catch (IOException | TimeoutException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public void resetECU(ECUResetMode mode) throws IOException, TimeoutException {
        try {
            try (var transaction = getSession().request(getECUComponent().getSendAddress(),
                    new UDSECUResetRequest(mode))) {
                transaction.join();
            }
        } catch (IOException | TimeoutException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public byte[] readDID(short did) throws IOException, TimeoutException {
        try {
            try (var transaction = getSession().request(getECUComponent().getSendAddress(),
                    new UDSReadDataByIDRequest(did))) {
                var response = transaction.get();
                return response.getData();
            }
        } catch (IOException | TimeoutException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private class KeepAliveThread extends Thread {
        @Override
        public void run() {
            synchronized (stateObject) {
                while (connectionMode != ConnectionMode.DISCONNECTED && session != null) {
                    long now = System.currentTimeMillis();
                    long deadline = lastFrameSent + getKeepAliveInterval();
                    if (now >= deadline) {
                        try {
                            keepAlive();
                            lastFrameSent = now;
                        } catch (IOException | TimeoutException e) {
                            Log.can().log(Level.WARNING, "Failed to send keep-alive message", e);
                        }
                    } else {
                        long toWait = deadline - now;

                        try {
                            stateObject.wait(toWait);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            }
        }
    }
}
