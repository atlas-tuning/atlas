package com.github.manevolent.atlas.uds.request;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.BitWriter;
import com.github.manevolent.atlas.Frame;
import com.github.manevolent.atlas.uds.UDSRequest;
import com.github.manevolent.atlas.uds.response.UDSReadDTCInformationResponse;

import java.io.IOException;

public class UDSReadDTCInformationRequest
        extends UDSRequest<UDSReadDTCInformationResponse> implements Frame  {

    @Override
    public void read(BitReader reader) throws IOException {

    }

    @Override
    public void write(BitWriter writer) throws IOException {

    }
}
