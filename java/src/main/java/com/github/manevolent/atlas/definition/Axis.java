package com.github.manevolent.atlas.definition;

import java.util.Collections;
import java.util.List;

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
