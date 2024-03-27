package com.github.manevolent.atlas.model;

import com.github.manevolent.atlas.model.subaru.SubaruDIMemoryEncryption;

import java.util.function.Supplier;

public enum MemoryEncryptionType {

    NONE("None", () -> null),
    SUBARU_DIT("Subaru DI (2015+)", new SubaruDIMemoryEncryption.Factory());

    private final MemoryEncryptionFactory factory;
    private final String name;

    MemoryEncryptionType(String name, MemoryEncryptionFactory factory) {
        this.name = name;
        this.factory = factory;
    }

    public MemoryEncryptionFactory getFactory() {
        return factory;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }


}
