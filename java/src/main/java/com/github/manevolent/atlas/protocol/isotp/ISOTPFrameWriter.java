package com.github.manevolent.atlas.protocol.isotp;

import com.github.manevolent.atlas.Address;
import com.github.manevolent.atlas.BasicFrame;

import com.github.manevolent.atlas.FrameWriter;
import com.github.manevolent.atlas.protocol.can.CANFrame;

import java.io.IOException;

public class ISOTPFrameWriter implements FrameWriter<BasicFrame> {
    private final FrameWriter<CANFrame> canWriter;

    public ISOTPFrameWriter(FrameWriter<CANFrame> canWriter) {
        this.canWriter = canWriter;
    }

    @Override
    public void write(Address address, BasicFrame frame) throws IOException {
        if (frame.getLength() <= 0)
            throw new IllegalArgumentException("Empty frame");

        byte[] data = frame.getData();
        int offs = 0;
        for (int index = 0; offs < data.length; index ++) {
            ISOTPDataSubFrame subFrame;
            int windowSize;

            if (offs == 0) {
                if (data.length <= 6) {
                    subFrame = new ISOTPSingleFrame();
                } else {
                    subFrame = new ISOTPFirstFrame();
                    ((ISOTPFirstFrame)subFrame).setTotalSize(data.length);
                }
                windowSize = 6;
            } else {
                subFrame = new ISOTPConsecutiveFrame();
                ((ISOTPConsecutiveFrame)subFrame).setIndex(index);
                windowSize = 7;
            }

            byte[] chunk = new byte[Math.min(data.length - offs, windowSize)];
            System.arraycopy(data, offs, chunk, 0, chunk.length);
            offs += chunk.length;
            subFrame.setData(chunk);

            ISOTPWireFrame wireFrame = new ISOTPWireFrame();
            wireFrame.setSubFrame(subFrame);

            CANFrame canFrame = new CANFrame();
            canFrame.setData(wireFrame.write());

            canWriter.write(address, canFrame);
        }
    }
}
