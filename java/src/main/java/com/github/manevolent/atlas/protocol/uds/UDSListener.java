package com.github.manevolent.atlas.protocol.uds;

public interface UDSListener {

    /**
     * Called when a UDS frame is read, but has not been handled yet
     * @param frame frame
     */
    void onUDSFrameRead(UDSFrame frame);

    /**
     * Called when a UDS frame is written successfully, but has not been answered yet
     * @param frame UDS frame that was sent
     */
    void onUDSFrameWrite(UDSFrame frame);

}
