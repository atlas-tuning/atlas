package com.github.manevolent.atlas.uds.request;

import com.github.manevolent.atlas.uds.UDSFrameType;
import com.github.manevolent.atlas.uds.UDSRequest;
import com.github.manevolent.atlas.uds.response.UDSAuthenticationResponse;

public class UDSAuthenticationRequest extends UDSRequest<UDSAuthenticationResponse> {
    @Override
    public UDSFrameType getType() {
        return UDSFrameType.AUTHENTICATION;
    }
}
