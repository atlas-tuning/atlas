package com.github.manevolent.atlas.subaru;

import com.github.manevolent.atlas.can.CanArbitrationId;
import com.github.manevolent.atlas.uds.UDSComponent;

public enum SubaruDITComponent implements UDSComponent {

    // Just a handful for now
    ENGINE_1(102, id(0x7A2), id(0x7AA), id(0x7DF)),
    ENGINE_2(103, id(0x7E0), id(0x7E8), id(0x7DF)),
    TRANSMISSION(201, id(0x7E1), id(0x7E9), id(0x7DF)),
    POWER_STEERING(1201, id(0x746), id(0x74E), null),
    KEYLESS_ACCESS(1301, id(0x751), id(0x759), null); // and push start

    private static CanArbitrationId id(int id) {
        return new CanArbitrationId(id);
    }

    private final int id;
    private final CanArbitrationId ecuAddress;
    private final CanArbitrationId funcEcuAdddress;
    private final CanArbitrationId toolAddress; // reply-to address

    SubaruDITComponent(int id,
                       CanArbitrationId ecuAddress,
                       CanArbitrationId funcEcuAdddress,
                       CanArbitrationId toolAddress) {
        this.id = id;
        this.ecuAddress = ecuAddress;
        this.funcEcuAdddress = funcEcuAdddress;
        this.toolAddress = toolAddress;
    }

    public int getId() {
        return id;
    }

    public CanArbitrationId getEcuAddress() {
        return ecuAddress;
    }

    public CanArbitrationId getFuncEcuAdddress() {
        return funcEcuAdddress;
    }

    public CanArbitrationId getToolAddress() {
        return toolAddress;
    }

    @Override
    public CanArbitrationId getSendAddress() {
        return getEcuAddress();
    }

    @Override
    public CanArbitrationId getReplyAddress() {
        return getToolAddress();
    }

}
