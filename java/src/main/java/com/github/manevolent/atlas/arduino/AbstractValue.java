package com.github.manevolent.atlas.arduino;

public abstract class AbstractValue implements Value {
    private final String name;
    private final boolean isStatic;

    public AbstractValue(String name, boolean isStatic) {
        this.name = name;
        this.isStatic = isStatic;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isStatic() {
        return isStatic;
    }
}
