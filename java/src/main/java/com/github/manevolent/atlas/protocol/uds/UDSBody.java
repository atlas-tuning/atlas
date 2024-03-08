package com.github.manevolent.atlas.protocol.uds;

import com.github.manevolent.atlas.Frame;

public abstract class UDSBody implements Frame {
    @Override
    public String toString() {
        String data;
        try {
            data = toHexString();
        } catch (UnsupportedOperationException ex) {
            data = "(unsupported)";
        }

        return "data=" + data;
    }
}
