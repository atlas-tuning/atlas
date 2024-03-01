package com.github.manevolent.atlas.uds.response;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.uds.UDSFrameType;
import com.github.manevolent.atlas.uds.UDSResponse;

public class UDSAuthenticationResponse extends UDSResponse {
    @Override
    public UDSFrameType getType() {
        return UDSFrameType.AUTHENTICATION;
    }
}
