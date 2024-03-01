package com.github.manevolent.atlas.subaru;

import com.github.manevolent.atlas.can.CANArbitrationId;
import com.github.manevolent.atlas.uds.UDSComponent;

import static com.github.manevolent.atlas.can.CANArbitrationId.id;

public enum SubaruDITComponent implements UDSComponent {
    BROADCAST(-1, id(0x7DF), null),
    CENTRAL_GATEWAY(4301, id(0x763), id(0x76B)),
    BODY_CONTROL(501, id(0x752), id(0x75A)),
    AIR_CONDITIONER(0, id(0x7C4), id(0x7CC)),
    HEADLIGHT(0, id(0x747), id(0x74F)),
    INFOTAINMENT(0, id(0x7D0), id(0x7D8)),
    TELEMATICS(0, id(0x776), id(0x77E)),
    BRAKE_CONTROL(0, id(0x7B0), id(0x7B8)), // ABS?
    FRONT_RELAY_CONTROL(4601, id(0x744), id(0x74C)),
    TIRE_PRESSURE_MONITOR(0, id(0x753), id(0x75B)),
    AIRBAG(0, id(0x780), id(0x788)),
    COMBINATION_METER(0, id(0x783),  id(0x78B)),
    ENGINE_1(102, id(0x7A2), id(0x7AA)),
    ENGINE_2(103, id(0x7E0),  id(0x7E8)),
    TRANSMISSION(201, id(0x7E1), id(0x7E9)),
    POWER_STEERING(1201, id(0x746), id(0x74E)),
    KEYLESS_ACCESS_1(1301, id(0x751), id(0x759)), // and push start
    KEYLESS_ACCESS_2(1302, id(0x7C1), id(0x7C9)),
    KEYLESS_ACCESS_3(1303, id(0x7B4), id(0x7BC));

    private final int id;

    private final CANArbitrationId sendAddress;
    private final CANArbitrationId replyAddress;

    SubaruDITComponent(int id,
                       CANArbitrationId sendAddress,
                       CANArbitrationId replyAddress) {
        this.id = id;
        this.sendAddress = sendAddress;
        this.replyAddress = replyAddress;
    }

    public int getId() {
        return id;
    }

    @Override
    public CANArbitrationId getSendAddress() {
        return sendAddress;
    }

    @Override
    public CANArbitrationId getReplyAddress() {
        return replyAddress;
    }

}
