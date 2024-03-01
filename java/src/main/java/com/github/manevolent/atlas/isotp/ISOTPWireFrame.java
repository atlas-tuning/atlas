package com.github.manevolent.atlas.isotp;

import com.github.manevolent.atlas.Frame;
import com.github.manevolent.atlas.BitReader;

import java.io.IOException;

public class ISOTPWireFrame {
    private final Frame parent;

    protected byte[] data;

    public ISOTPWireFrame(Frame parent) {
        this.parent = parent;
    }

    public ISOTPSubFrame getSubFrame() throws IOException {
        // See: https://www.csselectronics.com/pages/uds-protocol-tutorial-unified-diagnostic-services
        BitReader reader = parent.bitReader();

        byte code = (byte) reader.read(4);

        switch (code) {
            case 0x0: // Single frame
                byte sz = (byte) reader.read(4);
                byte[] data = new byte[sz];
                reader.read(data);
                return new ISOTPSingleFrame(data);
            case 0x1: // First frame
                int totalSize = (int) reader.read(12);
                byte[] firstData = new byte[6];
                reader.read(firstData);
                return new ISOTPFirstFrame(totalSize, firstData);
            case 0x2: // Consecutive frame
                int index = (int) reader.read(4);
                byte[] consData = new byte[7];
                reader.read(consData);
                return new ISOTPConsecutiveFrame(index, consData);
            case 0x3: // Flow control frame
                int flag = (int) reader.read(4);
                int blockSize = (int) reader.read(8);
                int separationTime = (int) reader.read(8);
                return new ISOTPFlowControlFrame(flag, blockSize, separationTime);
            default:
                throw new IllegalArgumentException("Unknown ISO-TP frame code: " + code);
        }
    }
}
