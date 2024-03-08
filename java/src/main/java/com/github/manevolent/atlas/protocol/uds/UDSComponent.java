package com.github.manevolent.atlas.protocol.uds;

import com.github.manevolent.atlas.protocol.can.CANArbitrationId;
import com.github.manevolent.atlas.protocol.j2534.J2534Device;

public interface UDSComponent {

    /**
     * Gets the address used to send UDS requests to
     * @return send address
     */
    CANArbitrationId getSendAddress();

    /**
     * Gets the address expected to receive UDS responses at
     * @return expected reply address
     */
    CANArbitrationId getReplyAddress();

    default J2534Device.ISOTPFilter toISOTPFilter() {
        return new J2534Device.ISOTPFilter(
                new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF },
                getReplyAddress() != null ? getReplyAddress().getData() : null,
                getSendAddress() != null ? getSendAddress().getData() : null
        );
    }

}
