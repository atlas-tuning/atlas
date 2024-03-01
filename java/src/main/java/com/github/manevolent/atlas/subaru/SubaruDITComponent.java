package com.github.manevolent.atlas.subaru;

import com.github.manevolent.atlas.can.CANArbitrationId;
import com.github.manevolent.atlas.j2534.J2534Device;
import com.github.manevolent.atlas.uds.UDSComponent;

import java.nio.ByteBuffer;

import static com.github.manevolent.atlas.can.CANArbitrationId.id;

public enum SubaruDITComponent implements UDSComponent {

    // Just a handful for now
    BROADCAST(-1, id(0x7DF), null),
    UNKNOWN_1(0, id(0x763), id(0x76B)), // CGW?
    UNKNOWN_2(0, id(0x752), id(0x75A)), // CGW?
    ENGINE_1(102, id(0x7A2), id(0x7AA)),
    ENGINE_2(103, id(0x7E0),  id(0x7E8)),
    TRANSMISSION(201, id(0x7E1), id(0x7E9)),
    POWER_STEERING(1201, id(0x746), id(0x74E)),
    KEYLESS_ACCESS(1301, id(0x751), id(0x759)); // and push start



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
