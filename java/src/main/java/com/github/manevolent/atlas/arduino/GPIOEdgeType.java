package com.github.manevolent.atlas.arduino;

public enum GPIOEdgeType {
    RISING(0x1),
    FALLING(0x2),
    EITHER(0x3);

    private final int flag;

    GPIOEdgeType(int flag) {
        this.flag = flag;
    }

    public int getFlag() {
        return flag;
    }
}
