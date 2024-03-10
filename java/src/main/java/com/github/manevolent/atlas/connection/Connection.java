package com.github.manevolent.atlas.connection;

import com.github.manevolent.atlas.model.MemoryParameter;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public interface Connection {

    boolean isConnected();

    ConnectionMode getConnectionMode();

    void changeConnectionMode(ConnectionMode newMode) throws IOException, TimeoutException;

    MemoryFrame readFrame(Collection<MemoryParameter> parameters);

}
