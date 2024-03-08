package com.github.manevolent.atlas.protocol.subaru.uds.response;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.protocol.uds.UDSResponse;

import java.io.IOException;

public class Subaru4Response extends UDSResponse {

    private byte[] data;

    @Override
    public void read(BitReader reader) throws IOException {
        data = reader.readRemaining();
    }

    @Override
    public byte[] getData() {
        return data;
    }

}
