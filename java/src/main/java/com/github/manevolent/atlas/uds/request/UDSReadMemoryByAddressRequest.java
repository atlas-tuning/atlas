package com.github.manevolent.atlas.uds.request;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.BitWriter;
import com.github.manevolent.atlas.uds.UDSRequest;
import com.github.manevolent.atlas.uds.response.UDSAuthenticationResponse;
import com.github.manevolent.atlas.uds.response.UDSReadMemoryByAddressResponse;

import java.io.IOException;

public class UDSReadMemoryByAddressRequest extends UDSRequest<UDSReadMemoryByAddressResponse> {
    private long address;
    private long size;

    public UDSReadMemoryByAddressRequest() {

    }

    public UDSReadMemoryByAddressRequest(long address, long size) {
        this.address = address;
        this.size = size;
    }

    @Override
    public void read(BitReader reader) throws IOException {
        int addressSize = (int) reader.read(4);
        int lengthSize = (int) reader.read(4);
        this.address = reader.read(addressSize * 8);
        this.size = reader.read(lengthSize * 8);
    }

    @Override
    public void write(BitWriter writer) throws IOException {
        writer.write(getBytes(address));
        writer.write(getBytes(size));
        writeParameter(address, writer);
        writeParameter(size, writer);
    }

    private static int getBytes(long parameter) {
        if (parameter < 0xFFL) {
            return 0x1;
        } else if (parameter < 0xFFFFL) {
            return 0x2;
        } else if (parameter < 0xFFFFFFFFL) {
            return 0x4;
        } else {
            return 0x8;
        }
    }

    private static void writeParameter(long parameter, BitWriter writer) throws IOException {
        if (parameter < 0xFFL) {
            writer.write((int) (parameter & 0xFF));
        } else if (parameter < 0xFFFFL) {
            writer.writeShort((short) (parameter & 0xFFFF));
        } else if (parameter < 0xFFFFFFFFL) {
            writer.writeInt((int) (parameter & 0xFFFFFFFFL));
        } else {
            writer.writeLong(parameter);
        }
    }

    @Override
    public String toString() {
        return "address=" + address + " size=" + size;
    }
}
