package com.github.manevolent.atlas.model.storage;

public enum ProjectStorageType {
    ZIP(new ZipProjectStorage.Factory());

    private final ProjectStorageFactory factory;

    ProjectStorageType(ProjectStorageFactory factory) {
        this.factory = factory;
    }

    public ProjectStorageFactory getStorageFactory() {
        return factory;
    }
}
