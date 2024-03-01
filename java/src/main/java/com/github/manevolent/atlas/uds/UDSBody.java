package com.github.manevolent.atlas.uds;

import com.github.manevolent.atlas.Frame;

public abstract class UDSBody implements Frame {
    public abstract byte getServiceId();
    public abstract UDSFrameType getType();
}
