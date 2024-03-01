package com.github.manevolent.atlas.isotp;

import java.nio.ByteBuffer;

public class ISOTPPeer {
    private final int arbitrationId;
    private final ByteBuffer buffer = ByteBuffer.allocate(4095);
    private int expected = -1;

    public ISOTPPeer(int arbitrationId) {
        this.arbitrationId = arbitrationId;
    }

    public ISOTPFrame handleFrame(ISOTPSubFrame subFrame) {
        if (subFrame instanceof ISOTPSingleFrame) {
            return ((ISOTPSingleFrame) subFrame).coalesce();
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
                byte[] reassembled = new byte[buffer.position()];
                System.arraycopy(buffer.array(), 0, reassembled, 0, buffer.position());
                buffer.position(0);
                expected = -1;
                return new ISOTPFrame(reassembled);
            }
        }

        return null;
    }
}
