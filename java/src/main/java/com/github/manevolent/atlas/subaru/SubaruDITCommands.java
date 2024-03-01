package com.github.manevolent.atlas.subaru;

import com.github.manevolent.atlas.Address;
import com.github.manevolent.atlas.ssm4.Crypto;
import com.github.manevolent.atlas.subaru.uds.request.SubaruReadDTCRequest;
import com.github.manevolent.atlas.subaru.uds.response.SubaruReadDTCResponse;
import com.github.manevolent.atlas.uds.UDSComponent;
import com.github.manevolent.atlas.uds.command.UDSDataByIdSupplier;
import com.github.manevolent.atlas.uds.command.UDSReadMemoryCommand;
import com.github.manevolent.atlas.uds.command.UDSSecurityAccessCommand;
import com.github.manevolent.atlas.uds.command.UDSSupplier;

import java.util.Set;

import static com.github.manevolent.atlas.subaru.SubaruDITComponent.*;

public final class SubaruDITCommands {

    public static final UDSDataByIdSupplier<Boolean> IGNITION_ON =
            new UDSDataByIdSupplier<>(ENGINE_1, 0x11C8) {
                @Override
                public Boolean handle(byte[] data) {
                    return data.length == 1 && data[0] == (byte) 0xFF;
                }
            };

    /**
     * Allows for a programming session and for flash to be written to
     */
    public static final UDSSecurityAccessCommand SECURITY_ACCESS_LEVEL_1 =
            new SubaruSecurityAccessCommandAES(0x1, ENGINE_1, Crypto.toByteArray("667E3078219976B4EDF3D43BD1D8FFC9"));

    /**
     * Allows you to write parameters (DIDs) to ECU, such as VIN
     */
    public static final UDSSecurityAccessCommand SECURITY_ACCESS_LEVEL_3 =
            new SubaruSecurityAccessCommandAES(0x3, ENGINE_1, Crypto.toByteArray("469A20AB308D5CA64BCD5BBE535BD85F"));

    /**
     * Unknown purpose
     */
    public static final UDSSecurityAccessCommand SECURITY_ACCESS_LEVEL_5 =
            new SubaruSecurityAccessCommandAES(0x5, ENGINE_1, Crypto.toByteArray("E8CC52D5D8F20706424813126FA7ABDD"));

    /**
     * Used on the CGW (Central Gateway) module
     */
    public static final UDSSecurityAccessCommand SECURITY_ACCESS_LEVEL_7 =
            new SubaruSecurityAccessCommandAES(0x7, CENTRAL_GATEWAY,
                    Crypto.toByteArray("7692E7932F23A901568DDFA5FF580625"));

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
