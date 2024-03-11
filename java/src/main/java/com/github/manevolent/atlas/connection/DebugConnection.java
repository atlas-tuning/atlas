package com.github.manevolent.atlas.connection;

import com.github.manevolent.atlas.logging.Log;
import com.github.manevolent.atlas.model.MemoryAddress;
import com.github.manevolent.atlas.model.MemoryParameter;

import com.github.manevolent.atlas.protocol.uds.UDSComponent;
import com.github.manevolent.atlas.protocol.uds.UDSProtocol;
import com.github.manevolent.atlas.protocol.uds.UDSSession;
import com.github.manevolent.atlas.protocol.uds.debug.DebugUDSSession;
import com.github.manevolent.atlas.protocol.uds.request.UDSDefineDataIdentifierRequest;
import com.github.manevolent.atlas.protocol.uds.request.UDSTesterPresentRequest;
import net.codecrete.usb.linux.IO;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

public class DebugConnection extends UDSConnection {
    private final Random random = new Random();
    private final Set<MemoryParameter> activeParameters = new LinkedHashSet<>();

    @Override
    public MemoryFrame readFrame(Collection<MemoryParameter> parameters) {
        MemoryFrame memoryFrame = new MemoryFrame();

        boolean changed = parameters.size() != activeParameters.size() ||
                parameters.stream().anyMatch(x -> !activeParameters.contains(x)) ||
                activeParameters.stream().anyMatch(x -> !parameters.contains(x));

        if (changed) {
            try (var transaction = getSession().request(DebugUDSSession.COMPONENT.getSendAddress(),
                    new UDSDefineDataIdentifierRequest(
                            0x2, 0xF300, new byte[]{}))) {
                // Do nothing
                transaction.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            // Ensure the order is preserved
            this.activeParameters.clear();
            this.activeParameters.addAll(new LinkedHashSet<>(parameters));
        }

        for (MemoryParameter parameter : parameters) {
            byte[] data = parameter.newBuffer();
            random.nextBytes(data);
            memoryFrame.setData(parameter, data);
        }

        return memoryFrame;
    }

    @Override
    protected UDSProtocol getProtocol() {
        return UDSProtocol.STANDARD;
    }

    @Override
    protected UDSComponent getECUComponent() {
        return DebugUDSSession.COMPONENT;
    }

    @Override
    public UDSSession connect() throws IOException {
        DebugUDSSession session = new DebugUDSSession();
        session.start();
        return session;
    }

    @Override
    public UDSSession spy() throws IOException, TimeoutException {
        return connect();
    }

    @Override
    public byte[] readMemory(MemoryAddress address, int length) throws IOException, TimeoutException {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }

        return new byte[length];
    }

    @Override
    protected void change(ConnectionMode newMode) throws IOException, TimeoutException {
        // Sure thing, bud!
        Log.can().log(Level.INFO, "Debug session changed to " + newMode);
    }

    @Override
    protected void keepAlive() throws IOException, TimeoutException {
        getSession().request(getECUComponent().getSendAddress(),
                new UDSTesterPresentRequest(new byte[] { (byte) 0xBE, (byte) 0xEE, (byte) 0xEF }));
    }
}
