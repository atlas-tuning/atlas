package com.github.manevolent.atlas.isotp;

import com.github.manevolent.atlas.Frame;

public class ISOTPSingleFrame extends ISOTPSubFrame implements Frame {
    private final byte[] data;

    public ISOTPSingleFrame(byte[] data) {
        this.data = data;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    public ISOTPFrame coalesce() {
        return new ISOTPFrame(data);
    }
}
