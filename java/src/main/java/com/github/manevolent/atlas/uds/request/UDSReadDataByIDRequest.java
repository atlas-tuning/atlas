package com.github.manevolent.atlas.uds.request;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.BitWriter;
import com.github.manevolent.atlas.uds.DataIdentifier;
import com.github.manevolent.atlas.uds.UDSFrameType;
import com.github.manevolent.atlas.uds.UDSRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class UDSReadDataByIDRequest extends UDSRequest {
    private int[] dids;

    public UDSReadDataByIDRequest(int[] dids) {
        this.dids = dids;
    }

    public UDSReadDataByIDRequest() {

    }

    @Override
    public UDSFrameType getType() {
        return UDSFrameType.READ_DATA_BY_ID;
    }

    @Override
    public void read(BitReader reader) throws IOException {
        int numDids = reader.remaining() / 16;
        dids = new int[numDids];
        for (int i = 0; i < numDids; i ++) {
            dids[i] = reader.readShort();
        }
    }

    @Override
    public void write(BitWriter writer) throws IOException {
        for (int did : dids) {
            writer.writeShort((short) did);
        }
    }

    @Override
    public String toString() {
        return "dids=" + Arrays.stream(dids)
                .mapToObj(did -> {
                    DataIdentifier found = DataIdentifier.findByDid((short)did);
                    return String.format("%04X(%s)", (short)did, found.text());
                })
                .collect(Collectors.joining(","));
    }
}
