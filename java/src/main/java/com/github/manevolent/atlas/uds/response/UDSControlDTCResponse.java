package com.github.manevolent.atlas.uds.response;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.uds.DTCControlMode;
import com.github.manevolent.atlas.uds.UDSFrameType;
import com.github.manevolent.atlas.uds.UDSRequest;
import com.github.manevolent.atlas.uds.UDSResponse;

import java.io.IOException;
import java.util.Arrays;

public class UDSControlDTCResponse extends UDSResponse {
    private int code;

    @Override
    public UDSFrameType getType() {
        return UDSFrameType.CONTROL_DTC_SETTINGS;
    }

    @Override
    public void read(BitReader reader) throws IOException {
        code = reader.readByte() & 0xFF;
    }

    @Override
    public String toString() {
        DTCControlMode found = Arrays.stream(DTCControlMode.values())
                .filter(sf -> sf.getCode() == this.code).findFirst()
                .orElse(null);

        if (found != null) {
            return found.name();
        } else {
            return String.format("Unknown 0x%02X", code);
        }
    }
}
