package com.github.manevolent.atlas.uds.request;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.BitWriter;
import com.github.manevolent.atlas.Frame;
import com.github.manevolent.atlas.uds.UDSRequest;
import com.github.manevolent.atlas.uds.response.UDSTesterPresentResponse;

import java.io.IOException;

public class UDSTesterPresentRequest extends UDSRequest<UDSTesterPresentResponse> implements Frame {
    private byte[] data;

    public UDSTesterPresentRequest() {
        data = new byte[0];
    }

    public UDSTesterPresentRequest(byte[] data) {
        this.data = data;
    }

    public UDSTesterPresentRequest(byte flag) {
        this.data = new byte[] { flag };
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
    public void write(BitWriter writer) throws IOException {
        writer.write(data);
    }

    @Override
    public String toString() {
        return toHexString();
    }
}
