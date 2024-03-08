package com.github.manevolent.atlas.protocol.isotp;

import com.github.manevolent.atlas.BitWriter;
import com.github.manevolent.atlas.Frame;
import com.github.manevolent.atlas.BitReader;

import java.io.IOException;

public class ISOTPWireFrame implements Frame {
    private ISOTPSubFrame subFrame;

    public void read(BitReader reader) throws IOException {
        // See: https://www.csselectronics.com/pages/uds-protocol-tutorial-unified-diagnostic-services
        byte code = (byte) reader.read(4);

        switch (code) {
            case 0x0: // Single frame
                setSubFrame(new ISOTPSingleFrame());
                break;
            case 0x1: // First frame
                this.subFrame = new ISOTPFirstFrame();
                break;
            case 0x2: // Consecutive frame
                this.subFrame = new ISOTPConsecutiveFrame();
                break;
            case 0x3: // Flow control frame
                this.subFrame = new ISOTPFlowControlFrame();
                break;
            default:
                throw new IllegalArgumentException("Unknown ISO-TP frame code: " + code);
        }

        getSubFrame().read(reader);
    }

    public void write(BitWriter writer) throws IOException {
        ISOTPSubFrame subFrame = getSubFrame();
        writer.writeNibble(subFrame.getCode());
        subFrame.write(writer);
    }

    public ISOTPSubFrame getSubFrame() {
        return this.subFrame;
    }

    public void setSubFrame(ISOTPSubFrame subFrame) {
        this.subFrame = subFrame;
    }
}
