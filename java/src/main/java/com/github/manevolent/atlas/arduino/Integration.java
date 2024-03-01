package com.github.manevolent.atlas.arduino;

public enum Integration {
    LINEAR(0x1),
    FLOOR(0x2),
    CEILING(0x3);

    private final int flag;

    Integration(int flag) {
        this.flag = flag;
    }

    public int getFlag() {
        return flag;
    }
}
