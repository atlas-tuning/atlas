package com.github.manevolent.atlas.uds.response;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.uds.UDSResponse;

import java.io.IOException;

// See: https://piembsystech.com/request-download-0x34-service-uds-protocol/
public class UDSUploadResponse extends UDSResponse {
    private long blockLength;

    @Override
    public void read(BitReader reader) throws IOException {
        /**
         * This parameter is used by the request download positive response
         * message to inform the client how many data bytes (maxNumberOfBlockLength)
         * to include in each TransferData request (0x36) service message from the client.
         * This length defines the total message length, including the SID and the
         * data parameters present in the TransferData request message (0x36).
         * This parameter allows the client to adapt to the receive buffer size
         * of the server before it starts transferring the data to the server.
         * A server is required to accept Transfer data requests that are equal in
         * length to its reported “MaxNumberOfBlockLength“.
         */
        int maxNumberOfBlockLength = (int) reader.read(4);

        reader.read(4); // reserved, this is always set to 0x0

        blockLength = (long) reader.read(maxNumberOfBlockLength * 8);
    }

    @Override
    public String toString() {
        return "blockLength=" + blockLength;
    }
}
