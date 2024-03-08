package com.github.manevolent.atlas.connection;

import com.github.manevolent.atlas.model.MemoryParameter;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public interface Connection {

    boolean isConnected();

    ConnectionMode getConnectionMode();

    void changeConnectionMode(ConnectionMode newMode) throws IOException, TimeoutException;

    Set<MemoryParameter> getParameters();

    void addParameter(MemoryParameter parameter);

    void removeParameter(MemoryParameter parameter);

    void addMemoryFrameListener(Consumer<MemoryFrame> listener);
    void removeMemoryFrameListener(Consumer<MemoryFrame> listener);

}
