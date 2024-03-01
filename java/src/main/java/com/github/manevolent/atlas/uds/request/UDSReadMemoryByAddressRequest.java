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
        this.sizeLength = (int) reader.read(4);
        this.addressLength = (int) reader.read(4);
        this.size = reader.read(sizeLength);
        this.address = reader.read(addressLength);
    }

    @Override
    public void write(BitWriter writer) throws IOException {
        writer.writeNibble((byte) sizeLength);
        writer.writeNibble((byte) addressLength);
        writer.writeLSB((int)size, sizeLength * 8);
        writer.writeLSB((int)address, addressLength * 8);
    }

    @Override
    public String toString() {
        return "address=" + address + " size=" + size;
    }
}
