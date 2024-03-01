package com.github.manevolent.atlas.uds.request;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.BitWriter;
import com.github.manevolent.atlas.Frame;
import com.github.manevolent.atlas.uds.UDSRequest;
import com.github.manevolent.atlas.uds.response.UDSCommunicationControlResponse;
import net.codecrete.usb.linux.IO;

import java.io.IOException;

// See: https://embetronicx.com/tutorials/automotive/uds-protocol/diagnostics-and-communication-management/#Communication_Control
public class UDSCommunicationControlRequest extends UDSRequest<UDSCommunicationControlResponse> implements Frame {
    private int communicationType;
    private byte[] data;

    public UDSCommunicationControlRequest() {
        this.data = new byte[0];
    }

    public UDSCommunicationControlRequest(int communicationType, byte[] data) {
        this.communicationType = communicationType;
        this.data = data;
    }

    public UDSCommunicationControlRequest(int communicationType) {
        this.communicationType = communicationType;
        this.data = new byte[0];
    }

    @Override
    public void read(BitReader reader) throws IOException {
        this.communicationType = reader.readByte() & 0xFF;
        this.data = reader.readRemaining();
    }

    @Override
    public void write(BitWriter writer) throws IOException {
        writer.write(this.communicationType);
        writer.write(data);
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
