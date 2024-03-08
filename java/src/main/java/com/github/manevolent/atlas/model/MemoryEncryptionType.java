package com.github.manevolent.atlas.model;

import com.github.manevolent.atlas.model.subaru.SubaruDITMemoryEncryption;

import java.util.function.Supplier;

public enum MemoryEncryptionType {

    NONE(() -> null),
    SUBARU_DIT(SubaruDITMemoryEncryption::new);

    private final Supplier<MemoryEncryption> instanceSupplier;

    MemoryEncryptionType(Supplier<MemoryEncryption> instanceSupplier) {
        this.instanceSupplier = instanceSupplier;
    }

    public MemoryEncryption create(Rom rom) {
        MemoryEncryption memoryEncryption = instanceSupplier.get();
        memoryEncryption.setEncryptionKeys(rom);
        return memoryEncryption;
    }
}
