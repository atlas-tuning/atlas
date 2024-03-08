package com.github.manevolent.atlas.protocol.subaru.uds.response;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.protocol.uds.UDSResponse;

import java.io.IOException;

public class SubaruStatus7Response extends UDSResponse {

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
        return String.format("code=0x%02X data=%s", code, toHexString());
    }

}
