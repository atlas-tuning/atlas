package com.github.manevolent.atlas.arduino;

public class Variable extends AbstractValue {
    private final float value;

    public Variable(String name, float value) {
        super(name, true);

        this.value = value;
    }

    @Override
    public float get() {
        return value;
    }
}
