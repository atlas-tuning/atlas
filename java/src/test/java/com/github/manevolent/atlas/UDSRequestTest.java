package com.github.manevolent.atlas;

import com.github.manevolent.atlas.protocol.uds.UDSQuery;
import com.github.manevolent.atlas.protocol.uds.UDSRequest;
import com.github.manevolent.atlas.protocol.uds.UDSResponse;
import com.github.manevolent.atlas.protocol.uds.UDSSide;
import com.github.manevolent.atlas.protocol.uds.request.UDSAuthenticationRequest;
import com.github.manevolent.atlas.protocol.uds.response.UDSAuthenticationResponse;
import org.junit.jupiter.api.Test;

import static com.github.manevolent.atlas.protocol.uds.UDSProtocol.STANDARD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UDSRequestTest {

    @Test
    public void testRequestClass_ResponseClassMatches() {
        Class<? extends UDSResponse> responseClass = UDSRequest.getResponseClass(UDSAuthenticationRequest.class);
        assertEquals(UDSAuthenticationResponse.class, responseClass);
    }

    @Test
    public void testSid_7thBitSet() {
        for (UDSQuery query : STANDARD.getQueries()) {
            Integer requestSid, responseSid;
            requestSid = query.getSid(UDSSide.REQUEST);
            responseSid = query.getSid(UDSSide.RESPONSE);
            if (requestSid != null) {
                assertEquals(0x0, (requestSid & 0x40));
            }
            if (responseSid != null && requestSid != null) {
                assertEquals(requestSid | 0x40, responseSid);
            } else if (responseSid != null) {
                assertEquals(responseSid & 0x40, 0x40);
            }
        }
    }

}
