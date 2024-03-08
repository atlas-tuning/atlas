package com.github.manevolent.atlas.connection;

import com.github.manevolent.atlas.model.MemoryParameter;
import com.github.manevolent.atlas.protocol.uds.UDSComponent;
import com.github.manevolent.atlas.protocol.uds.UDSProtocol;
import com.github.manevolent.atlas.protocol.uds.UDSSession;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

public abstract class UDSConnection implements Connection {
    private ConnectionMode connectionMode = ConnectionMode.DISCONNECTED;
    private Set<MemoryParameter> parameters = new LinkedHashSet<>();
    private Consumer<MemoryFrame> listener;
    private UDSSession session;

    @Override
    public boolean isConnected() {
        return session != null && getConnectionMode() != ConnectionMode.DISCONNECTED;
    }

    @Override
    public ConnectionMode getConnectionMode() {
        return connectionMode;
    }

    protected abstract UDSComponent[] getComponents();

    protected abstract UDSProtocol getProtocol();

    @Override
    public void changeConnectionMode(ConnectionMode newMode) {
        if (newMode == connectionMode) {
            return;
        }

        if (connectionMode == ConnectionMode.DISCONNECTED) {
            // Connect

        }


    }

    @Override
    public Set<MemoryParameter> getParameters() {
        return parameters;
    }

    @Override
    public void addParameter(MemoryParameter parameter) {

    }

    @Override
    public void removeParameter(MemoryParameter parameter) {

    }

    @Override
    public void setReadMemoryListener(Consumer<MemoryFrame> listener) {
        this.listener = listener;
    }
}
