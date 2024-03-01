package com.github.manevolent.atlas.uds.response;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.Frame;
import com.github.manevolent.atlas.uds.DataIdentifier;
import com.github.manevolent.atlas.uds.UDSFrameType;
import com.github.manevolent.atlas.uds.UDSResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class UDSReadDataByIDResponse extends UDSResponse implements Frame {
    private int did;
    private byte[] value;

    @Override
    public UDSFrameType getType() {
        return UDSFrameType.READ_DATA_BY_ID;
    }

    @Override
    public void read(BitReader reader) throws IOException {
        did = reader.readShort() & 0xFFFF;
        value = new byte[reader.remaining() / 8];
        reader.read(value);
    }

    @Override
    public byte[] getData() {
        return value;
    }

    @Override
    public String toString() {
        DataIdentifier found = DataIdentifier.findByDid((short)did);
        return String.format("%04X(%s) value=%s", (short)did, found.text(), Frame.toHexString(getData()));
    }
}
