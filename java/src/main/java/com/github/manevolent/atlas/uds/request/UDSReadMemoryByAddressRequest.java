package com.github.manevolent.atlas.uds.request;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.BitWriter;
import com.github.manevolent.atlas.uds.UDSRequest;
import com.github.manevolent.atlas.uds.response.UDSAuthenticationResponse;
import com.github.manevolent.atlas.uds.response.UDSReadMemoryByAddressResponse;

import java.io.IOException;

public class UDSReadMemoryByAddressRequest extends UDSRequest<UDSReadMemoryByAddressResponse> {
    private int addressLength;
    private int sizeLength;
    private long address;
    private long size;

    public UDSReadMemoryByAddressRequest() {

    }

    public UDSReadMemoryByAddressRequest(int addressLength, long address, int sizeLength, long size) {
        this.address = address;
        this.size = size;
        this.addressLength = addressLength;
        this.sizeLength = sizeLength;
    }

    @Override
    public void read(BitReader reader) throws IOException {
        this.addressLength = (int) reader.read(4);
        this.sizeLength = (int) reader.read(4);
        this.address = reader.read(addressLength);
        this.size = reader.read(sizeLength);
    }

    @Override
    public void write(BitWriter writer) throws IOException {
        writer.writeNibble((byte) addressLength);
        writer.writeNibble((byte) sizeLength);
        writer.writeLSB((int)address, addressLength * 8);
        writer.writeLSB((int)size, sizeLength * 8);
    }

    @Override
    public String toString() {
        return "address=" + address + " size=" + size;
    }
}
