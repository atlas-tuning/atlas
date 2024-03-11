package com.github.manevolent.atlas.protocol.uds.command;

import com.github.manevolent.atlas.protocol.uds.*;

import java.io.IOException;

public interface UDSCommand<R extends UDSRequest<S>, S extends UDSResponse> {

    UDSComponent getComponent();

    R newRequest() throws IOException;

    void handle(UDSSession session, S response) throws IOException;

    @SuppressWarnings("unchecked")
    default void execute(UDSSession session) throws IOException {
        UDSComponent component = getComponent();
        R request = newRequest();
        S response;
        try (UDSTransaction<R, S> transaction = session.request(component.getSendAddress(), request)) {
            response = transaction.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        handle(session, response);
    }

}
