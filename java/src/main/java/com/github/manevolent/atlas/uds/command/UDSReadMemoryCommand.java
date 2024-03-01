package com.github.manevolent.atlas.uds.command;

import com.github.manevolent.atlas.uds.UDSComponent;
import com.github.manevolent.atlas.uds.UDSSession;
import com.github.manevolent.atlas.uds.request.UDSReadMemoryByAddressRequest;
import com.github.manevolent.atlas.uds.response.UDSReadMemoryByAddressResponse;

import java.io.IOException;

public abstract class UDSReadMemoryCommand
        implements UDSCommand<UDSReadMemoryByAddressRequest, UDSReadMemoryByAddressResponse> {
    private final UDSComponent component;
    private final int memoryAddress;
    private final int memoryReadSize;

    public UDSReadMemoryCommand(UDSComponent component, int memoryAddress, int memoryReadSize) {
        this.component = component;
        this.memoryAddress = memoryAddress;
        this.memoryReadSize = memoryReadSize;
    }

    protected abstract int getMemoryAddressDataLength();
    protected abstract int getMemoryReadDataLength();

    @Override
    public UDSComponent getComponent() {
        return component;
    }

    @Override
    public UDSReadMemoryByAddressRequest newRequest() throws IOException {
        return new UDSReadMemoryByAddressRequest(
                getMemoryAddressDataLength(), memoryAddress,
                getMemoryReadDataLength(), memoryReadSize
        );
    }

    @Override
    public void handle(UDSSession session, UDSReadMemoryByAddressResponse response) throws IOException {

    }
}
