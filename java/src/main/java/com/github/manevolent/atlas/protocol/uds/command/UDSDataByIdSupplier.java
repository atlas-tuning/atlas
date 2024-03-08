package com.github.manevolent.atlas.protocol.uds.command;

import com.github.manevolent.atlas.protocol.uds.request.UDSReadDataByIDRequest;
import com.github.manevolent.atlas.protocol.uds.UDSComponent;

import com.github.manevolent.atlas.protocol.uds.response.UDSReadDataByIDResponse;

public abstract class UDSDataByIdSupplier<T> implements
        UDSSupplier<UDSReadDataByIDRequest, UDSReadDataByIDResponse, T> {

    private final UDSComponent component;
    private final int did;

    protected UDSDataByIdSupplier(UDSComponent component, int did) {
        this.component = component;
        this.did = did;
    }

    @Override
    public UDSComponent getComponent() {
        return component;
    }

    @Override
    public UDSReadDataByIDRequest newRequest() {
        return new UDSReadDataByIDRequest(new int[] { did });
    }

    @Override
    public T handle(UDSReadDataByIDResponse response) {
        return handle(response.getData());
    }

    protected abstract T handle(byte[] data);

}
