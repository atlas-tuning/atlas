package com.github.manevolent.atlas.subaru;

import com.github.manevolent.atlas.subaru.uds.request.SubaruStatus1Request;
import com.github.manevolent.atlas.subaru.uds.request.SubaruStatus3Request;
import com.github.manevolent.atlas.subaru.uds.request.SubaruStatus7Request;
import com.github.manevolent.atlas.uds.BasicUDSProtocol;
import com.github.manevolent.atlas.uds.UDSProtocol;
import com.github.manevolent.atlas.uds.UDSQuery;

public final class SubaruProtocols {

    public static final UDSProtocol DIT = new BasicUDSProtocol(
            UDSQuery.from("Subaru Status 0x1", 0x1, SubaruStatus1Request.class),
            UDSQuery.from("Subaru Status 0x3", 0x3, SubaruStatus3Request.class),
            UDSQuery.from("Subaru Status 0x7", 0x7, SubaruStatus7Request.class) // seen sent to transmission
    ).layer(UDSProtocol.STANDARD);

}
