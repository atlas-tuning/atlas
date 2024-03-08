package com.github.manevolent.atlas.protocol.uds.response;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.Frame;
import com.github.manevolent.atlas.protocol.uds.UDSResponse;

import java.io.IOException;

public class UDSClearDTCInformationResponse extends UDSResponse implements Frame {
    private byte[] data;

    @Override
    public void read(BitReader reader) throws IOException {
        data = reader.readRemaining();
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return "data=" + toHexString();
    }
}
