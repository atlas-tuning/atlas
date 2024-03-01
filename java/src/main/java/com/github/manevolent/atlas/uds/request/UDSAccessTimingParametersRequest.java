package com.github.manevolent.atlas.uds.request;

import com.github.manevolent.atlas.uds.UDSFrameType;
import com.github.manevolent.atlas.uds.UDSRequest;

public class UDSAccessTimingParametersRequest extends UDSRequest {

    @Override
    public UDSFrameType getType() {
        return UDSFrameType.ACCESS_TIMING_PARAMETERS;
    }

}
