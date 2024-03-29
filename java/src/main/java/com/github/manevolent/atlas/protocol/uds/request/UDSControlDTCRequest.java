package com.github.manevolent.atlas.protocol.uds.request;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.protocol.uds.DTCControlMode;
import com.github.manevolent.atlas.protocol.uds.UDSRequest;
import com.github.manevolent.atlas.protocol.uds.response.UDSControlDTCResponse;

import java.io.IOException;
import java.util.Arrays;

public class UDSControlDTCRequest extends UDSRequest<UDSControlDTCResponse> {
    private int code;

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
