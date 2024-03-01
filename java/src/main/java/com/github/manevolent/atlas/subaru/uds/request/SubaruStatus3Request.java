package com.github.manevolent.atlas.subaru.uds.request;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.subaru.uds.response.SubaruStatus3Response;
import com.github.manevolent.atlas.uds.UDSRequest;

import java.io.IOException;

public class SubaruStatus3Request extends UDSRequest<SubaruStatus3Response> {
    @Override
    public void read(BitReader reader) throws IOException {
    }
}
