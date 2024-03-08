package com.github.manevolent.atlas.connection;

import com.github.manevolent.atlas.model.MemoryParameter;

import java.util.Set;
import java.util.function.Consumer;

public interface Connection {

    boolean isConnected();

    ConnectionMode getConnectionMode();

    void changeConnectionMode(ConnectionMode newMode);

    Set<MemoryParameter> getParameters();

    void addParameter(MemoryParameter parameter);

    void removeParameter(MemoryParameter parameter);

    void setReadMemoryListener(Consumer<MemoryFrame> listener);

}
