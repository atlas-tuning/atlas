package com.github.manevolent.atlas.uds.request;

import com.github.manevolent.atlas.uds.UDSFrameType;
import com.github.manevolent.atlas.uds.UDSRequest;
import com.github.manevolent.atlas.uds.response.UDSAccessTimingParametersResponse;

public class UDSAccessTimingParametersRequest extends UDSRequest<UDSAccessTimingParametersResponse> {

    @Override
    public UDSFrameType getType() {
        return UDSFrameType.ACCESS_TIMING_PARAMETERS;
    }

}
