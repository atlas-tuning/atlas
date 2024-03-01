package com.github.manevolent.atlas.uds.request;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.Frame;
import com.github.manevolent.atlas.uds.UDSFrameType;
import com.github.manevolent.atlas.uds.UDSRequest;

import java.io.IOException;

public class UDSSecurityAccessRequest extends UDSRequest implements Frame {
    private int seed; // Supposed to be odd values
    private byte[] data; // Vendor-specific key

    @Override
    public UDSFrameType getType() {
        return UDSFrameType.SECURITY_ACCESS;
    }

    @Override
    public void read(BitReader reader) throws IOException {
        this.seed = reader.readByte();

        this.data = new byte[reader.remainingBytes()];
        reader.read(data);
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return "seed=" + seed + " key=" + toHexString();
    }
}
