package com.github.manevolent.atlas.protocol.uds.response;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.protocol.uds.UDSResponse;

import java.io.IOException;

public class UDSTransferResponse extends UDSResponse {
    private int index;
    private byte[] data;

    @Override
    public void read(BitReader reader) throws IOException {
        this.index = reader.readByte() & 0xFF;
        this.data = reader.readRemaining();
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return "index=" + index + " data=" + toHexString();
    }
}