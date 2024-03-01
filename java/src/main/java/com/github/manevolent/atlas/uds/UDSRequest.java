package com.github.manevolent.atlas.uds;

public abstract class UDSRequest extends UDSBody {

    public byte getServiceId() {
        return getType().getRequestSid();
    }

}
