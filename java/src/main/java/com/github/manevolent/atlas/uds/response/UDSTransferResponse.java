package com.github.manevolent.atlas.uds.response;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.uds.UDSResponse;

import java.io.IOException;

public class UDSTransferResponse extends UDSResponse {
    private int index;

    @Override
    public void read(BitReader reader) throws IOException {
        this.index = reader.readByte() & 0xFF;
    }

    @Override
    public String toString() {
        return "index=" + index;
    }
}