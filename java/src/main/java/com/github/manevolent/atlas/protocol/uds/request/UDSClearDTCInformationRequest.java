package com.github.manevolent.atlas.protocol.uds.request;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.BitWriter;
import com.github.manevolent.atlas.Frame;
import com.github.manevolent.atlas.protocol.uds.UDSRequest;
import com.github.manevolent.atlas.protocol.uds.response.UDSClearDTCInformationResponse;

import java.io.IOException;

public class UDSClearDTCInformationRequest extends UDSRequest<UDSClearDTCInformationResponse> implements Frame  {
    private byte[] data;

    public UDSClearDTCInformationRequest() {

    }

    public UDSClearDTCInformationRequest(byte[] data) {
        this.data = data;
    }

    @Override
    public void read(BitReader reader) throws IOException {
        data = reader.readRemaining();
    }

    @Override
    public void write(BitWriter writer) throws IOException {
        writer.write(data);
    }

    @Override
    public byte[] getData() {
        return data;
    }
}
