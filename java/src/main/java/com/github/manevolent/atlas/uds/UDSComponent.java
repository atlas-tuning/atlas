package com.github.manevolent.atlas.uds;

import com.github.manevolent.atlas.can.CanArbitrationId;

public interface UDSComponent {

    /**
     * Gets the address used to send UDS requests to
     * @return send address
     */
    CanArbitrationId getSendAddress();

    /**
     * Gets the address expected to receive UDS responses at
     * @return expected reply address
     */
    CanArbitrationId getReplyAddress();

}
