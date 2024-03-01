package com.github.manevolent.atlas.uds;

public abstract class UDSRequest<T extends UDSResponse> extends UDSBody {

    public byte getServiceId() {
        return getType().getRequestSid();
    }

}
