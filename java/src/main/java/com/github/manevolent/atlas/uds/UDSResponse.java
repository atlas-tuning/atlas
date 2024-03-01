package com.github.manevolent.atlas.uds;

public abstract class UDSResponse extends UDSBody {

    public byte getServiceId() {
        return getType().getResponseSid();
    }

}
