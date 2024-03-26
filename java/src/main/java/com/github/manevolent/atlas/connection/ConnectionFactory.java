package com.github.manevolent.atlas.connection;

import com.github.manevolent.atlas.protocol.j2534.J2534DeviceProvider;

import java.util.Collections;
import java.util.List;

public interface ConnectionFactory {

    /**
     * Creates a connection instance using a given provider
     * @param provider provider that will provide a J2534 device
     * @return connection
     */
    Connection createConnection(J2534DeviceProvider provider);

    /**
     * Gets the parameters for this connection that will be expected on a project.
     * @return list of parameters.
     */
    default List<ConnectionParameter> getParameters() {
        return Collections.emptyList();
    }

}
