package com.github.manevolent.atlas.connection;

import com.github.manevolent.atlas.logging.Log;
import com.github.manevolent.atlas.model.MemoryParameter;
import com.github.manevolent.atlas.protocol.uds.UDSComponent;
import com.github.manevolent.atlas.protocol.uds.UDSProtocol;
import com.github.manevolent.atlas.protocol.uds.UDSSession;
import com.github.manevolent.atlas.protocol.uds.request.UDSTesterPresentRequest;
import net.codecrete.usb.linux.IO;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class UDSConnection implements Connection {
    private ConnectionMode connectionMode = ConnectionMode.DISCONNECTED;
    private Set<MemoryParameter> parameters = new LinkedHashSet<>();
    private LinkedList<Consumer<MemoryFrame>> listeners = new LinkedList<>();
    private UDSSession session;
    private TesterPresentThread keepAliveThread ;
    private final Object stateObject = new Object();

    @Override
    public boolean isConnected() {
        return session != null && getConnectionMode() != ConnectionMode.DISCONNECTED;
    }

    @Override
    public ConnectionMode getConnectionMode() {
        return connectionMode;
    }

    protected final UDSSession getSession() {
        return session;
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
            if (connectionMode == ConnectionMode.DISCONNECTED) {
                stateObject.notifyAll();
                session = connect();
            } else if (newMode == ConnectionMode.DISCONNECTED) {
                if (session != null) {
                    session.close();
                }
                connectionMode = ConnectionMode.DISCONNECTED;
                stateObject.notifyAll();
                return;
            }

            change(newMode);
            connectionMode = newMode;

            if (keepAliveThread == null || !keepAliveThread.isAlive()) {
                keepAliveThread = new TesterPresentThread();
                keepAliveThread.setName("UDSTesterPresent");
                keepAliveThread.setDaemon(true);
                keepAliveThread.start();
            }
        }
    }

    protected abstract UDSSession connect() throws IOException;

    protected abstract void change(ConnectionMode newMode) throws IOException, TimeoutException;

    protected abstract void keepAlive() throws IOException, TimeoutException;

    private class TesterPresentThread extends Thread {
        @Override
        public void run() {
            synchronized (stateObject) {
                while (connectionMode != ConnectionMode.DISCONNECTED && session != null) {
                    try {
                        keepAlive();
                    } catch (IOException | TimeoutException e) {
                        Log.can().log(Level.WARNING, "Failed to send tester present message", e);
                    }

                    try {
                        stateObject.wait(1000L);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }
    }
}
