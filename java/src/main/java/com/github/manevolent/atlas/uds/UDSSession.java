package com.github.manevolent.atlas.uds;

import java.io.IOException;

public interface UDSSession {

    UDSTransaction request(UDSRequest request) throws IOException;

}
