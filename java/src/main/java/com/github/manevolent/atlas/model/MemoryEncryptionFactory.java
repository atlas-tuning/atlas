package com.github.manevolent.atlas.model;

import java.util.Collections;
import java.util.List;

public interface MemoryEncryptionFactory {

    default List<PropertyDefinition> getPropertyDefinitions() {
        return Collections.emptyList();
    }

    MemoryEncryption create();

    default MemoryEncryption create(Project project) {
        MemoryEncryption memoryEncryption = create();
        if (memoryEncryption == null) {
            return null;
        }

        memoryEncryption.setEncryptionKeys(project);
        return memoryEncryption;
    }

}
