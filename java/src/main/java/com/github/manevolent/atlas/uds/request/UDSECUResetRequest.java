package com.github.manevolent.atlas.uds.request;

import com.github.manevolent.atlas.uds.UDSFrameType;
import com.github.manevolent.atlas.uds.UDSRequest;
import com.github.manevolent.atlas.uds.response.UDSECUResetResponse;

public class UDSECUResetRequest extends UDSRequest<UDSECUResetResponse> {
    @Override
    public UDSFrameType getType() {
        return UDSFrameType.ECU_RESET;
    }
}
