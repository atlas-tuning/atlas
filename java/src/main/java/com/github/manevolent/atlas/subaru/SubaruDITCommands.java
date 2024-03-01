package com.github.manevolent.atlas.subaru;

import com.github.manevolent.atlas.Address;
import com.github.manevolent.atlas.BitWriter;
import com.github.manevolent.atlas.ssm4.Crypto;
import com.github.manevolent.atlas.subaru.uds.request.SubaruReadDTCRequest;
import com.github.manevolent.atlas.subaru.uds.response.SubaruReadDTCResponse;
import com.github.manevolent.atlas.uds.UDSComponent;
import com.github.manevolent.atlas.uds.UDSSession;
import com.github.manevolent.atlas.uds.command.UDSDataByIdSupplier;
import com.github.manevolent.atlas.uds.command.UDSRoutineCommand;
import com.github.manevolent.atlas.uds.command.UDSSecurityAccessCommand;
import com.github.manevolent.atlas.uds.command.UDSSupplier;
import com.github.manevolent.atlas.uds.response.UDSRoutineControlResponse;

import java.io.IOException;
import java.util.Set;

import static com.github.manevolent.atlas.subaru.SubaruDITComponent.ENGINE_1;
import static com.github.manevolent.atlas.subaru.SubaruDITComponent.ENGINE_2;

public final class SubaruDITCommands {

    public static final UDSDataByIdSupplier<Boolean> IGNITION_ON =
            new UDSDataByIdSupplier<>(ENGINE_1, 0x11C8) {
                @Override
                public Boolean handle(byte[] data) {
                    return data.length == 1 && data[0] == (byte) 0xFF;
                }
            };

    public static final UDSSecurityAccessCommand SECURITY_ACCESS_7_8 =
            new SubaruSecurityAccessCommand(0x7, ENGINE_2, Crypto.toByteArray("7692E7932F23A901568DDFA5FF580625"));

    public static final UDSSecurityAccessCommand SECURITY_ACCESS_1_2 =
            new SubaruSecurityAccessCommand(0x1, ENGINE_2, Crypto.toByteArray("667E3078219976B4EDF3D43BD1D8FFC9"));

    public static final UDSSupplier<SubaruReadDTCRequest, SubaruReadDTCResponse, Set<Short>>
            READ_DTC = new UDSSupplier<>() {
        @Override
        public UDSComponent getComponent() {
            return ENGINE_1;
        }

        @Override
        public Address getSendAddress() {
            return ENGINE_1.getSendAddress(); // Broadcast
        }

        @Override
        public SubaruReadDTCRequest newRequest() {
            return new SubaruReadDTCRequest();
        }

        @Override
        public Set<Short> handle(SubaruReadDTCResponse response) {
            return response.getDtcs();
        }
    };

}
