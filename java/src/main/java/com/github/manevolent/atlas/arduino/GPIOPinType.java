package com.github.manevolent.atlas.arduino;

public enum GPIOPinType {
    ANALOG(0x2),
    DIGITAL(0x1),
    PWM(0x3);

    private final int flag;

    GPIOPinType(int flag) {
        this.flag = flag;
    }

    public int getFlag() {
        return flag;
    }
}
