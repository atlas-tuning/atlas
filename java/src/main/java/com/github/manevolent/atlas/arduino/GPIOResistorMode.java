package com.github.manevolent.atlas.arduino;

public enum GPIOResistorMode {
    NONE(0x1),
    PULL_UP(0x3),
    PULL_DOWN(0x2);

    private final int flag;

    GPIOResistorMode(int flag) {
        this.flag = flag;
    }

    public int getFlag() {
        return flag;
    }
}
