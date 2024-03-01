package com.github.manevolent.atlas.isotp;

import com.github.manevolent.atlas.Frame;

public class ISOTPFrame implements Frame {
    private final byte[] reassembled;

    public ISOTPFrame(byte[] reassembled) {
        this.reassembled = reassembled;
    }

    @Override
    public byte[] getData() {
        return reassembled;
    }

    @Override
    public String toString() {
        return toHexString();
    }
}
