package com.github.manevolent.atlas.protocol.uds.request;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.BitWriter;
import com.github.manevolent.atlas.protocol.uds.UDSRequest;
import com.github.manevolent.atlas.protocol.uds.response.UDSUploadResponse;

import java.io.IOException;

// See: https://piembsystech.com/request-download-0x34-service-uds-protocol/
public class UDSUploadRequest extends UDSRequest<UDSUploadResponse> {
    private int dataEncryption;
    private int dataCompression;
    private int memoryIdentifier;

    private int memoryAddressBytes;
    private long memoryAddress;
    private int memorySizeBytes;
    private long memorySize;

    public UDSUploadRequest() {

    }

    public UDSUploadRequest(int memoryIdentifier,
                            int dataCompression, int dataEncryption, int memoryAddressBytes, long memoryAddress,
                            int memorySizeBytes, long memorySize) {
        this.memoryIdentifier = memoryIdentifier;
        this.dataCompression = dataCompression;
        this.dataEncryption = dataEncryption;
        this.memoryAddress = memoryAddress;
        this.memorySize = memorySize;
        this.memorySizeBytes = memorySizeBytes;
        this.memoryAddressBytes = memoryAddressBytes;
    }

    @Override
    public void read(BitReader reader) throws IOException {
        this.dataCompression = (int) reader.read(4);
        this.dataEncryption = (int) reader.read(4);

        int memorySizeBytes = (int) reader.read(4);
        int memoryAddressBytes = (int) reader.read(4);

        this.memoryAddress = reader.read(memoryAddressBytes * 8);
        this.memorySize = reader.read(memorySizeBytes * 8);
    }

    @Override
    public void write(BitWriter writer) throws IOException {
        writer.writeNibble((byte) (dataCompression & 0xFF));
        writer.writeNibble((byte) (dataEncryption & 0xFF));
        writer.writeNibble((byte) (memorySizeBytes & 0xFF));
        writer.writeNibble((byte) (memoryAddressBytes & 0xFF));
        writer.writeLSB((int) memoryAddress, memoryAddressBytes * 8);
        writer.writeLSB((int) memorySize, memorySizeBytes * 8);
    }

    @Override
    public String toString() {
        return "comp=" + dataCompression + " crypto=" + dataEncryption
                + " addr=" + memoryAddress + " sz=" + memorySize
                + " memid=" + memoryIdentifier;
    }
}
