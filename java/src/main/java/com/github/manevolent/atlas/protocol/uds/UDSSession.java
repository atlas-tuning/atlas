package com.github.manevolent.atlas.protocol.uds;

import com.github.manevolent.atlas.Address;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public interface UDSSession extends Closeable {
    <T extends UDSResponse> UDSTransaction<T> request(Address address, UDSRequest<T> request) throws IOException, TimeoutException;
}
