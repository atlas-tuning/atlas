package com.github.manevolent.atlas.uds.response;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.uds.NegativeResponseCode;
import com.github.manevolent.atlas.uds.UDSFrameType;
import com.github.manevolent.atlas.uds.UDSResponse;

import java.io.IOException;
import java.util.Arrays;

public class UDSNegativeResponse extends UDSResponse {
    private byte rejectedSid;
    private NegativeResponseCode responseCode;

    @Override
    public UDSFrameType getType() {
        return UDSFrameType.NEGATIVE_RESPONSE;
    }

    @Override
    public void read(BitReader reader) throws IOException {
        this.rejectedSid = reader.readByte();

        final byte responseCodeByte = reader.readByte();

        this.responseCode = Arrays.stream(NegativeResponseCode.values()).filter(rc -> rc.getCode() == responseCodeByte)
                .findFirst().orElseThrow(() -> new UnsupportedOperationException("Unsupported response code "
                        + responseCodeByte));
    }

    public byte getRejectedSid() {
        return rejectedSid;
    }

    public NegativeResponseCode getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(NegativeResponseCode responseCode) {
        this.responseCode = responseCode;
    }

    @Override
    public String toString() {
        return "sid=" + rejectedSid + " reason=" + responseCode.name();
    }
}
