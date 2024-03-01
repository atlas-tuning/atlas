package com.github.manevolent.atlas.subaru;

import com.github.manevolent.atlas.ssm4.Crypto;
import com.github.manevolent.atlas.uds.command.UDSDataByIdSupplier;
import com.github.manevolent.atlas.uds.command.UDSSecurityAccessCommand;

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


}
