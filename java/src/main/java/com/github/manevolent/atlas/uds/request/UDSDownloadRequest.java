package com.github.manevolent.atlas.uds.request;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.uds.DataIdentifier;
import com.github.manevolent.atlas.uds.UDSFrameType;
import com.github.manevolent.atlas.uds.UDSRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

// See: https://piembsystech.com/request-download-0x34-service-uds-protocol/
public class UDSDownloadRequest extends UDSRequest {
    private int dataEncryption;
    private int dataCompression;
    private int memoryIdentifier;

    private long memoryAddress;
    private long memorySize;

    @Override
    public UDSFrameType getType() {
        return UDSFrameType.DOWNLOAD;
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
    public String toString() {
        return "comp=" + dataCompression + " crypto=" + dataEncryption
                + " memid=" + memoryIdentifier
                + " addr=" + memoryAddress + " sz=" + memorySize;
    }
}
