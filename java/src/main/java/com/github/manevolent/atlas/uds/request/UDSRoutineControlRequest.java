package com.github.manevolent.atlas.uds.request;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.Frame;
import com.github.manevolent.atlas.uds.DataIdentifier;
import com.github.manevolent.atlas.uds.RoutineControlSubFunction;
import com.github.manevolent.atlas.uds.UDSRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

// See: https://piembsystech.com/routinecontrol-0x31-service-uds-protocol/
public class UDSRoutineControlRequest extends UDSRequest implements Frame {
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
