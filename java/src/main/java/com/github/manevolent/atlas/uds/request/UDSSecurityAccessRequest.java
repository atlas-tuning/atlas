package com.github.manevolent.atlas.uds.request;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.BitWriter;
import com.github.manevolent.atlas.Frame;
import com.github.manevolent.atlas.uds.UDSRequest;
import com.github.manevolent.atlas.uds.response.UDSSecurityAccessResponse;

import java.io.IOException;

public class UDSSecurityAccessRequest
        extends UDSRequest<UDSSecurityAccessResponse> implements Frame {
    private int seed; // Supposed to be odd values
    private byte[] data; // Vendor-specific key

    public UDSSecurityAccessRequest() {

    }

    public UDSSecurityAccessRequest(int seed, byte[] data) {
        this.seed = seed;
        this.data = data;
    }

    public UDSSecurityAccessRequest(int seed) {
        this.seed = seed;
        this.data = new byte[0];
    }


    @Override
    public void read(BitReader reader) throws IOException {
        this.seed = reader.readByte();

        this.data = new byte[reader.remainingBytes()];
        reader.read(data);
    }

    @Override
    public void write(BitWriter writer) throws IOException {
        writer.write(this.seed);
        writer.write(this.data);
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
