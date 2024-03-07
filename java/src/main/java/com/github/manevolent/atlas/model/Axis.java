package com.github.manevolent.atlas.model;

public enum Axis {
    X(0),
    Y(1);

    private final int index;

    Axis(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
