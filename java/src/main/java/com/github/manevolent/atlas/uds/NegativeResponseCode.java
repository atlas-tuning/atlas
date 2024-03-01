package com.github.manevolent.atlas.uds;

public enum NegativeResponseCode {
        GENERAL_REJECT(0x10),
        SERVICE_NOT_SUPPORTED(0x11),
        SUB_FUNCTION_NOT_SUPPORTED(0x12),
        INVALID_MESSAGE_LEN_OR_FORMAT(0x13),
        RESPONSE_TOO_LONG(0x14),
        BUSY_REPEAT_REQUEST(0x21),
        CONDITIONS_NOT_CORRECT(0x22),
        REQUEST_SEQUENCE_ERROR(0x24),
        NO_RESPONSE_FROM_SUBNET_COMPONENT(0x25),
        FAILURE_PREVENTS_EXECUTION(0x26),
        REQUEST_OUT_OF_RANGE(0x31),
        SECURITY_ACCESS_DENIED(0x33),
        INVALID_KEY(0x35),
        EXCEEDED_NUM_ATTEMPTS(0x36),
        TIME_DELAY_NOT_EXPIRED(0x37),
        UL_DL_NOT_ACCEPTED(0x70),
        TRANSFER_DATA_SUSPENDED(0x71),
        PROGRAMMING_FAILURE(0x72),
        WRONG_BLOCK_SEQ_COUNTER(0x73),
        RESPONSE_PENDING(0x78),
        SUB_FUNC_NOT_SUPPORTED_IN_SESSION(0x7E),
        SERVICE_NOT_SUPPORTED_IN_SESSION(0x7F),
        RPM_TOO_HIGH(0x81),
        RPM_TOO_LOW(0x82),
        ENGINE_RUNNING(0x83),
        ENGINE_NOT_RUNNING(0x84),
        ENGINE_RUNTIME_TOO_LOW(0x85),
        TEMP_TOO_HIGH(0x86),
        TEMP_TOO_LOW(0x87),
        SPEED_TOO_HIGH(0x88),
        SPEED_TOO_LOW(0x89),
        THROTTLE_POS_TOO_HIGH(0x8A),
        THROTTLE_POS_TOO_LOW(0x8B),
        TRANS_RANGE_NOT_IN_NEUTRAL(0x8C),
        TRANS_RANGE_NOT_IN_GEAR(0x8D),
        BRAKE_SWITCHES_NOT_CLOSED(0x8F),
        SHIFT_LEVER_NOT_IN_PARK(0x90),
        CLUTCH_LOCKED(0x91),
        VOLTAGE_TOO_HIGH(0x92),
        VOLTAGE_TOO_LOW(0x93),
        MANUFAC_SPECIFC_1(0xF0),
        MANUFAC_SPECIFC_2(0xF1),
        MANUFAC_SPECIFC_3(0xF2),
        MANUFAC_SPECIFC_4(0xF3),
        MANUFAC_SPECIFC_5(0xF4),
        MANUFAC_SPECIFC_6(0xF5),
        MANUFAC_SPECIFC_7(0xF6),
        MANUFAC_SPECIFC_8(0xF7),
        MANUFAC_SPECIFC_9(0xF8),
        MANUFAC_SPECIFC_10(0xF9),
        MANUFAC_SPECIFC_11(0xFA),
        MANUFAC_SPECIFC_12(0xFB),
        MANUFAC_SPECIFC_13(0xFC),
        MANUFAC_SPECIFC_14(0xFD),
        MANUFAC_SPECIFC_15(0xFE);

        private final byte code;

        NegativeResponseCode(int code) {
            this.code = (byte) code;

        }

        public byte getCode() {
            return code;
        }

}