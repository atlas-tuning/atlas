package com.github.manevolent.atlas.protocol.uds.response;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.Frame;
import com.github.manevolent.atlas.protocol.uds.RoutineControlSubFunction;
import com.github.manevolent.atlas.protocol.uds.UDSResponse;

import java.io.IOException;
import java.util.Arrays;

// See: https://piembsystech.com/routinecontrol-0x31-service-uds-protocol/
public class UDSRoutineControlResponse extends UDSResponse implements Frame {
    private int controlFunction;
    private int routineId;
    private byte[] data;

    @Override
    public void read(BitReader reader) throws IOException {
        controlFunction = reader.readByte() & 0xFF;
        routineId = reader.readByte() & 0xFF;
        data = reader.readRemaining();
    }

    @Override
    public byte[] getData() {
        return data;
    }

    public int getRoutineId() {
        return routineId;
    }

    public int getControlFunction() {
        return controlFunction;
    }

    @Override
    public String toString() {
        String controlRoutine = Arrays.stream(RoutineControlSubFunction.values())
                .filter(sf -> sf.getCode() == this.controlFunction)
                .map(Enum::name)
                .findFirst()
                .orElse(Integer.toString(this.controlFunction));

        return "func=" + controlRoutine + " routineId=" + routineId + " data=" + toHexString();
    }

}
