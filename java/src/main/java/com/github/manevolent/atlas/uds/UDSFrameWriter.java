package com.github.manevolent.atlas.uds;

import com.github.manevolent.atlas.Address;
import com.github.manevolent.atlas.BasicFrame;
import com.github.manevolent.atlas.FrameWriter;

import java.io.IOException;

public class UDSFrameWriter implements FrameWriter<UDSBody> {
    private final FrameWriter<BasicFrame> transport;
    private final UDSProtocol protocol;

    public UDSFrameWriter(FrameWriter<BasicFrame> transport, UDSProtocol protocol) {
        this.transport = transport;
        this.protocol = protocol;
    }

    @Override
    public void write(Address address, UDSBody body) throws IOException {
        UDSFrame frame = new UDSFrame(protocol, body);


        System.out.println(address.toString() + " 0x" + Integer.toHexString(frame.getServiceId()) + " " +
                body.getClass().getSimpleName() + " " + body.toString());

        frame.setAddress(address);
        transport.write(address, BasicFrame.from(frame));
    }

    public void write(UDSComponent component, UDSBody body) throws IOException {
        write(component.getSendAddress(), body);
    }
}
