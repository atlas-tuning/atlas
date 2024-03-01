package com.github.manevolent.atlas.subaru;

import com.github.manevolent.atlas.uds.command.UDSReadMemoryCommand;

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
