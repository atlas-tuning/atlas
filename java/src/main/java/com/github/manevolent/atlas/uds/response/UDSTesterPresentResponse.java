package com.github.manevolent.atlas.uds.response;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.Frame;
import com.github.manevolent.atlas.uds.UDSFrameType;
import com.github.manevolent.atlas.uds.UDSResponse;

import java.io.IOException;

public class UDSTesterPresentResponse extends UDSResponse implements Frame {
    private byte[] data;

    @Override
    public UDSFrameType getType() {
        return UDSFrameType.TESTER_PRESENT;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public void read(BitReader reader) throws IOException {
        this.data = reader.readRemaining();
    }

    @Override
    public String toString() {
        return toHexString();
    }
}
