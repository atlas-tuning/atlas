package com.github.manevolent.atlas.protocol.uds;

import com.github.manevolent.atlas.Address;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public interface UDSSession extends Closeable {

    <Q extends UDSRequest<T>, T extends UDSResponse>
        UDSTransaction<Q, T> request(Address destination, Q request) throws IOException, TimeoutException;

    void addListener(UDSListener listener);

    boolean removeListener(UDSListener listener);

    boolean hasListener(UDSListener listener);

}
