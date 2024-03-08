package com.github.manevolent.atlas.protocol.isotp;

import com.github.manevolent.atlas.Frame;

public abstract class ISOTPSubFrame implements Frame {
    public abstract byte getCode();
}
