package com.github.manevolent.atlas.uds.request;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.Frame;
import com.github.manevolent.atlas.uds.UDSRequest;
import com.github.manevolent.atlas.uds.response.UDSTransferResponse;

import java.io.IOException;

public class UDSTransferRequest extends UDSRequest<UDSTransferResponse> implements Frame {
    private int index;
    private int address;
    private byte[] data;

    @Override
    public void read(BitReader reader) throws IOException {
        this.index = reader.readByte() & 0xFF;
        this.address = reader.readInt();

        this.data = new byte[256];
        reader.read(data);
    }

    public int getAddress() {
        return address;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return "index=" + index + " addr=" + address + " data=" + toHexString();
    }
}
