package com.github.manevolent.atlas.subaru;

import com.github.manevolent.atlas.subaru.uds.request.Subaru4Request;
import com.github.manevolent.atlas.subaru.uds.request.SubaruStatus1Request;
import com.github.manevolent.atlas.subaru.uds.request.SubaruReadDTCRequest;
import com.github.manevolent.atlas.subaru.uds.request.SubaruStatus7Request;
import com.github.manevolent.atlas.uds.BasicUDSProtocol;
import com.github.manevolent.atlas.uds.UDSProtocol;
import com.github.manevolent.atlas.uds.UDSQuery;

public final class SubaruProtocols {

    public static final UDSProtocol DIT = new BasicUDSProtocol(
            UDSQuery.from("Subaru Status 0x1", 0x1, SubaruStatus1Request.class),
            UDSQuery.from("Subaru Read DTC", 0x3, SubaruReadDTCRequest.class),
            UDSQuery.from("Subaru Unknown 4", 0x4, Subaru4Request.class),
            UDSQuery.from("Subaru Read DTC 2", 0x7, SubaruStatus7Request.class) // seen sent to transmission
    ).layer(UDSProtocol.STANDARD);

}
