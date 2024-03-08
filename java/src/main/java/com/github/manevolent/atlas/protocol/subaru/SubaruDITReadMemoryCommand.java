package com.github.manevolent.atlas.protocol.subaru;

import com.github.manevolent.atlas.protocol.uds.command.UDSReadMemoryCommand;

public class SubaruDITReadMemoryCommand extends UDSReadMemoryCommand {
    public SubaruDITReadMemoryCommand(SubaruDITComponent component, int memoryAddress) {
        super(component, memoryAddress, 0x7);
    }

    @Override
    protected int getMemoryReadDataLength() {
        return 1;
    }

    @Override
    protected int getMemoryAddressDataLength() {
        return 4;
    }
}
