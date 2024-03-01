package com.github.manevolent.atlas.uds.command;

import com.github.manevolent.atlas.uds.UDSComponent;
import com.github.manevolent.atlas.uds.UDSSession;
import com.github.manevolent.atlas.uds.UDSTransaction;
import com.github.manevolent.atlas.uds.request.UDSSecurityAccessRequest;
import com.github.manevolent.atlas.uds.response.UDSSecurityAccessResponse;

import java.io.IOException;


public abstract class UDSSecurityAccessCommand implements UDSCommand<UDSSecurityAccessRequest, UDSSecurityAccessResponse> {
    private final int seed;
    private final UDSComponent component;

    public UDSSecurityAccessCommand(int seed, UDSComponent component) {
        this.seed = seed;
        this.component = component;
    }

    public int getSeed() {
        return seed;
    }

    @Override
    public UDSComponent getComponent() {
        return component;
    }

    @Override
    public UDSSecurityAccessRequest newRequest() {
        return new UDSSecurityAccessRequest(seed, new byte[0]);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handle(UDSSession session, UDSSecurityAccessResponse response) throws IOException {
        UDSSecurityAccessRequest answer = answer(response);
        try (UDSTransaction<UDSSecurityAccessResponse> response2 =
                     session.request(getComponent().getSendAddress(), answer)) {
            handle(response2.get());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract UDSSecurityAccessRequest answer(UDSSecurityAccessResponse challenge);
    protected abstract void handle(UDSSecurityAccessResponse result);
}
