package com.github.manevolent.atlas.subaru.uds.request;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.BitWriter;
import com.github.manevolent.atlas.subaru.uds.response.SubaruStatus1Response;

import com.github.manevolent.atlas.uds.UDSRequest;

import java.io.IOException;

public class SubaruStatus1Request extends UDSRequest<SubaruStatus1Response> {

    private int code;

    public SubaruStatus1Request() {

    }

    public SubaruStatus1Request(int code) {
        this.code = code;
    }

    @Override
    public void read(BitReader reader) throws IOException {
        code = reader.readByte() & 0xFF;
    }

    @Override
    public void write(BitWriter writer) throws IOException {
        writer.write(code);
    }

    @Override
    public String toString() {
        return String.format("code=0x%02X", code);
    }

}
