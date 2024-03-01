package com.github.manevolent.atlas.uds;

import com.github.manevolent.atlas.BitReader;

import java.io.IOException;

public class UDSUnknownBody extends UDSBody {
    private byte[] data;

    @Override
    public void read(BitReader reader) throws IOException {
        this.data = reader.readRemaining();
    }

    @Override
    public byte[] getData() {
        return data;
    }
}
