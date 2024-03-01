package com.github.manevolent.atlas.arduino;

public enum TableType {

    ARITHMETIC(0x1),
    STATE_VOLATILE(0x2),
    STATE_NON_VOLATILE(0x3);

    private final int flag;

    TableType(int flag) {
        this.flag = flag;
    }

    public int getFlag() {
        return flag;
    }

}
