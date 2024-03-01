package com.github.manevolent.atlas.uds.request;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.uds.DiagnosticSessionType;
import com.github.manevolent.atlas.uds.UDSRequest;
import com.github.manevolent.atlas.uds.response.UDSDiagSessionControlResponse;

import java.io.IOException;
import java.util.Arrays;

public class UDSDiagSessionControlRequest extends UDSRequest<UDSDiagSessionControlResponse> {
    private int code;

    @Override
    public void read(BitReader reader) throws IOException {
        code = reader.readByte() & 0xFF;
    }

    @Override
    public String toString() {
        DiagnosticSessionType found = Arrays.stream(DiagnosticSessionType.values())
                .filter(sf -> sf.getCode() == this.code).findFirst()
                .orElse(null);

        if (found != null) {
            return found.name();
        } else {
            return String.format("Unknown 0x%02X", code);
        }
    }
}
