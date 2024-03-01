package com.github.manevolent.atlas.uds.request;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.Frame;
import com.github.manevolent.atlas.uds.UDSRequest;

import java.io.IOException;

public class UDSTesterPresentRequest extends UDSRequest implements Frame {
    private byte[] data;

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
