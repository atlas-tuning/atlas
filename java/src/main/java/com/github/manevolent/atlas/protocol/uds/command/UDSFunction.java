package com.github.manevolent.atlas.protocol.uds.command;

import com.github.manevolent.atlas.protocol.uds.*;


import java.io.IOException;

public interface UDSFunction<R extends UDSRequest<S>, S extends UDSResponse, P, T> {

    UDSComponent getComponent();

    R newRequest(P parameter);

    T handle(S response) throws IOException;

    @SuppressWarnings("unchecked")
    default T execute(UDSSession session, P parameter) throws IOException {
        UDSComponent component = getComponent();
        R request = newRequest(parameter);
        S response;
        try (UDSTransaction<R, S> transaction = session.request(component.getSendAddress(), request)) {
            response = transaction.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return handle(response);
    }

}
