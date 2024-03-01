package com.github.manevolent.atlas.isotp;

import com.github.manevolent.atlas.BasicFrame;

import com.github.manevolent.atlas.FrameWriter;
import com.github.manevolent.atlas.can.CanFrame;

import java.io.IOException;

public class ISOTPFrameWriter implements FrameWriter<BasicFrame> {
    private final FrameWriter<CanFrame> canWriter;

    public ISOTPFrameWriter(FrameWriter<CanFrame> canWriter) {
        this.canWriter = canWriter;
    }

    @Override
    public void write(BasicFrame frame) throws IOException {
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

            CanFrame canFrame = new CanFrame();
            canFrame.setData(wireFrame.write());

            canWriter.write(canFrame);
        }
    }
}
