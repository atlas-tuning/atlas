package com.github.manevolent.atlas.uds;

public enum DTCControlMode {
    DTC_ON(0x01),
    DTC_OFF(0x02);

    private int code;
    DTCControlMode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
