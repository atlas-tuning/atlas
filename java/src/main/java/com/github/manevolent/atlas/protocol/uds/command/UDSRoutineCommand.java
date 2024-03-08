package com.github.manevolent.atlas.protocol.uds.command;

import com.github.manevolent.atlas.BitWriter;
import com.github.manevolent.atlas.protocol.uds.request.UDSRoutineControlRequest;
import com.github.manevolent.atlas.protocol.uds.UDSComponent;

import com.github.manevolent.atlas.protocol.uds.response.UDSRoutineControlResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class UDSRoutineCommand implements UDSCommand<UDSRoutineControlRequest, UDSRoutineControlResponse> {
    private final UDSComponent component;
    private final int function;
    private final int routineId;

    protected UDSRoutineCommand(UDSComponent component, int function, int routineId) {
        this.component = component;
        this.function = function;
        this.routineId = routineId;
    }

    protected abstract void newRequest(BitWriter writer) throws IOException;

    @Override
    public UDSComponent getComponent() {
        return component;
    }

    @Override
    public UDSRoutineControlRequest newRequest() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitWriter bitWriter = new BitWriter(baos);
        newRequest(bitWriter);
        return new UDSRoutineControlRequest(function, routineId, baos.toByteArray());
    }
}
