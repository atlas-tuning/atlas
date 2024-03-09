package com.github.manevolent.atlas.protocol.isotp;

import com.github.manevolent.atlas.protocol.can.CANArbitrationId;

import java.nio.ByteBuffer;

public class ISOTPPeer {
    private final int arbitrationId;
    private final ByteBuffer buffer = ByteBuffer.allocate(8192);
    private int expected = -1;

    public ISOTPPeer(int arbitrationId) {
        this.arbitrationId = arbitrationId;
    }

    public ISOTPFrame handleFrame(ISOTPSubFrame subFrame) {
        if (subFrame instanceof ISOTPSingleFrame) {
            return ((ISOTPSingleFrame) subFrame).coalesce(new CANArbitrationId(arbitrationId));
        } else if (subFrame instanceof ISOTPFirstFrame) {
            ISOTPFirstFrame firstFrame = (ISOTPFirstFrame) subFrame;
            if (expected > 0) {
                // Got a first frame, but we weren't ready for it
                return null;
            }
            expected = firstFrame.getTotalSize();
            buffer.put(firstFrame.getData());
        } else if (subFrame instanceof ISOTPConsecutiveFrame) {
            if (expected <= 0) {
                // Got a consecutive frame, but we weren't ready for it
                return null;
            }

            ISOTPConsecutiveFrame consFrame = (ISOTPConsecutiveFrame) subFrame;
            buffer.put(consFrame.getData());
            if (buffer.position() >= expected) {
                // Assemble
                byte[] reassembled = new byte[expected];
                System.arraycopy(buffer.array(), 0, reassembled, 0, expected);
                buffer.position(0);
                expected = -1;
                return new ISOTPFrame(new CANArbitrationId(arbitrationId), reassembled);
            }
        }

        return null;
    }
}
