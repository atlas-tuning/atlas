package com.github.manevolent.atlas.subaru.uds.request;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.subaru.uds.response.SubaruStatus1Response;

import com.github.manevolent.atlas.uds.UDSRequest;

import java.io.IOException;

public class SubaruStatus1Request extends UDSRequest<SubaruStatus1Response> {

    private int code;

    @Override
    public void read(BitReader reader) throws IOException {
        code = reader.readByte() & 0xFF;
    }

    @Override
    public String toString() {
        return String.format("code=0x%02X", code);
    }

}
