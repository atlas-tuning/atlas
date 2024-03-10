package com.github.manevolent.atlas.model;

import com.github.manevolent.atlas.model.subaru.SubaruDITMemoryEncryption;

import java.util.function.Supplier;

public enum MemoryEncryptionType {

    NONE("None", () -> null),
    SUBARU_DIT("Subaru DIT (2015+)", SubaruDITMemoryEncryption::new);

    private final Supplier<MemoryEncryption> instanceSupplier;
    private final String name;

    MemoryEncryptionType(String name, Supplier<MemoryEncryption> instanceSupplier) {
        this.name = name;
        this.instanceSupplier = instanceSupplier;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public MemoryEncryption create(Project project) {
        MemoryEncryption memoryEncryption = instanceSupplier.get();
        memoryEncryption.setEncryptionKeys(project);
        return memoryEncryption;
    }
}
