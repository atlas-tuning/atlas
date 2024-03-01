package com.github.manevolent.atlas.uds.response;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.Frame;
import com.github.manevolent.atlas.uds.DiagnosticSessionType;
import com.github.manevolent.atlas.uds.UDSResponse;

import java.io.IOException;
import java.util.Arrays;

public class UDSDiagSessionControlResponse extends UDSResponse implements Frame {
    private int code;
    private byte[] data;

    @Override
    public void read(BitReader reader) throws IOException {
        code = reader.readByte() & 0xFF;
        data = reader.readRemaining();
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        DiagnosticSessionType found = Arrays.stream(DiagnosticSessionType.values())
                .filter(sf -> sf.getCode() == this.code).findFirst()
                .orElse(null);

        if (found != null) {
            return found.name() + " data=" + toHexString();
        } else {
            return String.format("Unknown 0x%02X", code) + " data=" + toHexString();
        }
    }
}
