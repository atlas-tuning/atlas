package com.github.manevolent.atlas.connection;

import com.github.manevolent.atlas.protocol.j2534.J2534DeviceProvider;

public interface ConnectionFactory {

    /**
     * Creates a connection instance using a given provider
     * @param provider provider that will provide a J2534 device
     * @return connection
     */
    Connection createConnection(J2534DeviceProvider provider);

}
