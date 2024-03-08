package com.github.manevolent.atlas.protocol.uds.response;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.BitWriter;
import com.github.manevolent.atlas.protocol.uds.UDSResponse;

import java.io.IOException;

public class UDSDefineDataIdentifierResponse extends UDSResponse {
    private int function;
    private int did;

    public UDSDefineDataIdentifierResponse() {
    }

    public UDSDefineDataIdentifierResponse(int function, int did) {
        this.function = function;
        this.did = did;
    }

    @Override
    public void read(BitReader reader) throws IOException {
        function = reader.readUByte();
        did = reader.readUShort();
    }

    @Override
    public void write(BitWriter writer) throws IOException {
        writer.write(function & 0xFF);
        writer.writeShort((short) (did & 0xFFFF));
    }

    @Override
    public String toString() {
        return "func=" + function + " did=" + Integer.toHexString(did);
    }

}
