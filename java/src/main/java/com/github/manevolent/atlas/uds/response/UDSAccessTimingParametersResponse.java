package com.github.manevolent.atlas.uds.response;

import com.github.manevolent.atlas.uds.UDSFrameType;
import com.github.manevolent.atlas.uds.UDSResponse;

public class UDSAccessTimingParametersResponse extends UDSResponse {
    @Override
    public UDSFrameType getType() {
        return UDSFrameType.ACCESS_TIMING_PARAMETERS;
    }
}
