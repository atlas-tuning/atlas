package com.github.manevolent.atlas.protocol.j2534;

// See: https://www.opusivs.com/support/j2534-support/passthru-sae-j2534-dll/possible-error-codes
public enum J2534Error {
    STATUS_NOERROR(0x00),
    ERR_NOT_SUPPORTED(0x01),
    ERR_INVALID_CHANNEL_ID(0x02),
    ERR_INVALID_PROTOCOL_ID(0x03),
    ERR_NULL_PARAMETER(0x04),
    ERR_INVALID_IOCTL_VALUE(0x05),
    ERR_INVALID_FLAGS(0x06),
    ERR_FAILED(0x07),
    ERR_DEVICE_NOT_CONNECTED(0x08),
    ERR_TIMEOUT(0x09),
    ERR_INVALID_MSG(0x0A),
    ERR_INVALID_TIME_INTERVAL(0x0B),
    ERR_EXCEEDED_LIMIT(0x0C),
    ERR_INVALID_MSG_ID(0x0D),
    ERR_DEVICE_IN_USE(0x0E),
    ERR_INVALID_IOCTL_ID(0x0F),
    ERR_BUFFER_EMPTY(0x10),
    ERR_BUFFER_FULL(0x11),
    ERR_BUFFER_OVERFLOW(0x12),
    ERR_PIN_INVALID(0x13),
    ERR_CHANNEL_IN_USE(0x14),
    ERR_MSG_PROTOCOL_ID(0x15),
    ERR_INVALID_FILTER_ID(0x16),
    ERR_NO_FLOW_CONTROL(0x17),
    ERR_NOT_UNIQUE(0x18),
    ERR_INVALID_BAUD_RATE(0x19),
    ERR_INVALID_DEVICE_ID(0x1A);

    private int code;
    J2534Error(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
