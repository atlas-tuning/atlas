package com.github.manevolent.atlas.protocol.uds.request;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.BitWriter;
import com.github.manevolent.atlas.protocol.uds.DiagnosticSessionType;
import com.github.manevolent.atlas.protocol.uds.UDSRequest;
import com.github.manevolent.atlas.protocol.uds.response.UDSReadDTCResponse;

import java.io.IOException;

public class UDSReadDTCRequest extends UDSRequest<UDSReadDTCResponse> {
    private int code;

    public UDSReadDTCRequest() {

    }

    public UDSReadDTCRequest(int code) {
        this.code = code;
    }

    public UDSReadDTCRequest(DiagnosticSessionType type) {
        this.code = type.getCode();
    }

    @Override
    public void read(BitReader reader) throws IOException {
        code = reader.readByte() & 0xFF;
    }

    @Override
    public void write (BitWriter writer) throws IOException {
        writer.write(code & 0xFF);
    }

    @Override
    public String toString() {
        return String.format("0x%02X", code);
    }
}
