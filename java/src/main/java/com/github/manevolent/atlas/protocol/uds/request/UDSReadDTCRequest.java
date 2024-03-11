package com.github.manevolent.atlas.protocol.uds.request;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.BitWriter;
import com.github.manevolent.atlas.protocol.uds.DiagnosticSessionType;
import com.github.manevolent.atlas.protocol.uds.UDSRequest;
import com.github.manevolent.atlas.protocol.uds.response.UDSReadDTCResponse;

import java.io.IOException;

// See: https://piembsystech.com/read-dtc-information-service-0x19-uds-protocol/
public class UDSReadDTCRequest extends UDSRequest<UDSReadDTCResponse> {
    private int code;
    private byte[] data;

    public UDSReadDTCRequest() {

    }

    public UDSReadDTCRequest(int code) {
        this.code = code;
    }

    public UDSReadDTCRequest(int code, byte[] data) {
        this.code = code;
        this.data = data;
    }

    public UDSReadDTCRequest(int code, byte mask) {
        this.code = code;
        this.data = new byte[] { mask };
    }

    public UDSReadDTCRequest(DiagnosticSessionType type) {
        this.code = type.getCode();
    }

    @Override
    public void read(BitReader reader) throws IOException {
        code = reader.readByte() & 0xFF;
        data = reader.readRemaining();
    }

    @Override
    public void write (BitWriter writer) throws IOException {
        writer.write(code & 0xFF);

        if (data != null) {
            writer.write(data);
        }
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return String.format("func=0x%02X data=%s", code, toHexString());
    }
}
