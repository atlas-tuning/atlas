package com.github.manevolent.atlas.uds.command;

import com.github.manevolent.atlas.Address;
import com.github.manevolent.atlas.uds.*;

import java.io.IOException;

public interface UDSSupplier<R extends UDSRequest<S>, S extends UDSResponse, T> {

    UDSComponent getComponent();

    default Address getSendAddress() {
        return getComponent().getSendAddress();
    }

    R newRequest();

    T handle(S response);

    @SuppressWarnings("unchecked")
    default T execute(UDSSession session) throws IOException {
        UDSComponent component = getComponent();
        R request = newRequest();
        S response;
        try (UDSTransaction<S> transaction = session.request(getSendAddress(), request)) {
            response = transaction.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return handle(response);
    }

}
