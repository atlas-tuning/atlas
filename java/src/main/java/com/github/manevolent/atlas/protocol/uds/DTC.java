package com.github.manevolent.atlas.protocol.uds;

public class DTC {

    public static final byte MASK_TEST_FAILED = 0x1;
    public static final byte MASK_TEST_FAILED_THIS_OPERATION_CYCLE = 1 << 0x01;
    public static final byte MASK_PENDING = 1 << 0x02;
    public static final byte MASK_CONFIRMED = 1 << 0x03;
    public static final byte MASK_TEST_NOT_COMPLETED_SINCE_LAST_CLEAR = 1 << 0x04;
    public static final byte MASK_TEST_FAILED_SINCE_LAST_CLEAR = 1 << 0x05;
    public static final byte MASK_TEST_NOT_COMPLETED_THIS_OPERATION_CYCLE = 1 << 0x06;
    public static final byte MASK_WARNING_INDICATOR_REQUESTED = (byte) (1 << 0x07);

    public static final byte SUB_FUNCTION_NUM_DTCS_BY_MASK = 0x01;
    public static final byte REPORT_DTC_BY_MASK = 0x02;
    // More exist, but not using

}
