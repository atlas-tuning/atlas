package com.github.manevolent.atlas.connection;

import com.github.manevolent.atlas.protocol.subaru.SubaruProtocols;
import com.github.manevolent.atlas.protocol.uds.UDSComponent;
import com.github.manevolent.atlas.protocol.uds.UDSProtocol;

import static com.github.manevolent.atlas.protocol.subaru.SubaruDITComponent.*;
import static com.github.manevolent.atlas.protocol.subaru.SubaruDITComponent.CENTRAL_GATEWAY;

public class SubaruDITConnection extends UDSConnection {

    @Override
    protected UDSComponent[] getComponents() {
        return new UDSComponent[] { ENGINE_1, ENGINE_2, BODY_CONTROL, CENTRAL_GATEWAY };
    }

    @Override
    protected UDSProtocol getProtocol() {
        return SubaruProtocols.DIT;
    }
}
