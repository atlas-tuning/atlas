package com.github.manevolent.atlas.uds;

import com.github.manevolent.atlas.BitReader;

import java.io.IOException;

public abstract class UDSBody {
    public byte[] getData() {
        throw new UnsupportedOperationException(getClass().getName() + " does not support getData()");
    }

    public void read(BitReader reader) throws IOException {
        throw new UnsupportedOperationException(getClass().getName() + " does not support read()");
    }
}
