package com.github.manevolent.atlas.protocol.uds;

import com.github.manevolent.atlas.Address;
import com.github.manevolent.atlas.BasicFrame;
import com.github.manevolent.atlas.FrameWriter;
import com.github.manevolent.atlas.logging.Log;

import java.io.IOException;
import java.util.logging.Level;

public class UDSFrameWriter implements FrameWriter<UDSBody> {
    private final FrameWriter<BasicFrame> transport;
    private final UDSProtocol protocol;

    public UDSFrameWriter(FrameWriter<BasicFrame> transport, UDSProtocol protocol) {
        this.transport = transport;
        this.protocol = protocol;
    }

    protected void onFrameWrite(UDSFrame frame) {

    }

    @Override
    public void write(Address address, UDSBody body) throws IOException {
        UDSFrame frame = new UDSFrame(protocol, body);
        frame.setAddress(address);
        Log.can().log(Level.FINER, frame.toString());
        transport.write(address, BasicFrame.from(frame));
        onFrameWrite(frame);
    }

    public void write(UDSComponent component, UDSBody body) throws IOException {
        write(component.getSendAddress(), body);
    }
}
