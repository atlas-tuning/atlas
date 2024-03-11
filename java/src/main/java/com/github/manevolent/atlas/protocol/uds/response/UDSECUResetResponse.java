package com.github.manevolent.atlas.protocol.uds.response;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.protocol.uds.ECUResetMode;
import com.github.manevolent.atlas.protocol.uds.UDSResponse;

import java.io.IOException;
import java.util.Arrays;

public class UDSECUResetResponse extends UDSResponse {
    private int resetMode;

    public UDSECUResetResponse() {

    }

    public UDSECUResetResponse(int mode) {
        this.resetMode = mode;
    }

    public UDSECUResetResponse(ECUResetMode mode) {
        this.resetMode = mode.getCode();
    }

    @Override
    public void read(BitReader reader) throws IOException {
        this.resetMode = reader.readByte();
    }

    @Override
    public String toString() {
        String resetMode = Arrays.stream(ECUResetMode.values())
                .filter(sf -> sf.getCode() == this.resetMode)
                .map(Enum::toString)
                .findFirst()
                .orElse(Integer.toString(this.resetMode));

        return "mode=" + resetMode;
    }
}
