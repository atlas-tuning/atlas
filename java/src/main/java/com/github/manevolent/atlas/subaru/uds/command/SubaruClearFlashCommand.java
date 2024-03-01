package com.github.manevolent.atlas.subaru.uds.command;

import com.github.manevolent.atlas.BitWriter;
import com.github.manevolent.atlas.Frame;
import com.github.manevolent.atlas.uds.UDSComponent;
import com.github.manevolent.atlas.uds.UDSSession;
import com.github.manevolent.atlas.uds.command.UDSRoutineCommand;
import com.github.manevolent.atlas.uds.response.UDSRoutineControlResponse;

import java.io.IOException;
import java.util.Arrays;

import static com.github.manevolent.atlas.uds.RoutineControlSubFunction.START_ROUTINE;

public class SubaruClearFlashCommand extends UDSRoutineCommand {
    private final int startAddress, endAddress;

    public SubaruClearFlashCommand(UDSComponent component, int startAddress, int endAddress) {
        super(component, START_ROUTINE.getCode(), 0xFF);
        this.startAddress = startAddress;
        this.endAddress = endAddress;
    }

    @Override
    public void handle(UDSSession session, UDSRoutineControlResponse response) throws IOException {
        if (response.getControlFunction() != START_ROUTINE.getCode()) {
            throw new IllegalStateException("Unexpected clear flash control function: " +
                    response.getControlFunction());
        }

        if (response.getRoutineId() != 0xFF) {
            throw new IllegalStateException("Unexpected clear flash routine ID: " +
                    response.getRoutineId());
        }

        if (!Arrays.equals(response.getData(), new byte[1])) {
            throw new IllegalStateException("Unexpected clear flash response: " +
                    Frame.toHexString(response.getData()));
        }
    }

    @Override
    protected void newRequest(BitWriter writer) throws IOException {
        writer.write(0x00); // this may be the data to set the following range to

        writer.writeNibble((byte) 0x4);
        writer.writeNibble((byte) 0x4);

        writer.writeInt(startAddress);
        writer.writeInt(endAddress);
    }
}
