package com.github.manevolent.atlas.uds;

import com.github.manevolent.atlas.Address;

import java.io.IOException;

public interface UDSSession {
    <T extends UDSResponse> UDSTransaction request(Address address, UDSRequest<T> request) throws IOException;

}
