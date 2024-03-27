package com.github.manevolent.atlas.model;

public enum MemoryType {
    BOOTLOADER("Bootloader"),
    CODE("Code Flash"),
    RAM("RAM");

    private final String name;

    MemoryType(String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return name;
    }
}
