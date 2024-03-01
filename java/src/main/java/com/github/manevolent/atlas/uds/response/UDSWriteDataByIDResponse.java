package com.github.manevolent.atlas.uds.response;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.BitWriter;
import com.github.manevolent.atlas.Frame;
import com.github.manevolent.atlas.uds.DataIdentifier;
import com.github.manevolent.atlas.uds.UDSResponse;

import java.io.IOException;

public class UDSWriteDataByIDResponse extends UDSResponse implements Frame {
    private int did;

    @Override
    public void read(BitReader reader) throws IOException {
        did = reader.readShort() & 0xFFFF;
    }

    @Override
    public void write(BitWriter writer) throws IOException {
        writer.writeShort((short) (did & 0xFFFF));
    }

    @Override
    public byte[] getData() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        DataIdentifier found = DataIdentifier.findByDid((short)did);
        return String.format("%04X(%s)", (short)did, found.text());
    }
}
