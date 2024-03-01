package com.github.manevolent.atlas.uds.response;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.Frame;
import com.github.manevolent.atlas.uds.UDSFrameType;
import com.github.manevolent.atlas.uds.UDSResponse;

import java.io.IOException;

public class UDSReadDTCInformationResponse extends UDSResponse implements Frame {
    private byte[] data;

    @Override
    public UDSFrameType getType() {
        return UDSFrameType.READ_DTC_INFORMATION;
    }

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
