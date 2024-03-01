package com.github.manevolent.atlas.uds.command;

import com.github.manevolent.atlas.uds.UDSComponent;

import com.github.manevolent.atlas.uds.request.UDSReadDataByIDRequest;
import com.github.manevolent.atlas.uds.response.UDSReadDataByIDResponse;

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
