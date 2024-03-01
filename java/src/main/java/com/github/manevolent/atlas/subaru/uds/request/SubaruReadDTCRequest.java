package com.github.manevolent.atlas.subaru.uds.request;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.BitWriter;
import com.github.manevolent.atlas.subaru.uds.response.SubaruReadDTCResponse;
import com.github.manevolent.atlas.uds.UDSRequest;

import java.io.IOException;

public class SubaruReadDTCRequest extends UDSRequest<SubaruReadDTCResponse> {
    @Override
    public void read(BitReader reader) throws IOException {
    }
    @Override
    public void write(BitWriter writer) throws IOException {
    }
}
