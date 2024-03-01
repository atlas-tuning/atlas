package com.github.manevolent.atlas.uds.request;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.Frame;
import com.github.manevolent.atlas.uds.UDSRequest;

import java.io.IOException;

// See: https://embetronicx.com/tutorials/automotive/uds-protocol/diagnostics-and-communication-management/#Communication_Control
public class UDSCommunicationControlRequest extends UDSRequest implements Frame {
    private int communicationType;
    private byte[] data;

    @Override
    public void read(BitReader reader) throws IOException {
        this.communicationType = reader.readByte() & 0xFF;
        this.data = reader.readRemaining();
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return "type=" + communicationType + " data=" + toHexString();
    }
}
