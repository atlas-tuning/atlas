package com.github.manevolent.atlas.protocol.subaru.uds.request;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.BitWriter;
import com.github.manevolent.atlas.protocol.subaru.uds.response.SubaruStatus7Response;
import com.github.manevolent.atlas.protocol.uds.UDSRequest;

import java.io.IOException;

/**
 * Seems functionally identical to read DTC
 */
public class SubaruStatus7Request extends UDSRequest<SubaruStatus7Response> {
    @Override
    public void read(BitReader reader) throws IOException {
    }

    @Override
    public void write(BitWriter writer) throws IOException {
    }
}
