package com.github.manevolent.atlas.model;

public final class PropertyDefinition {
    private final boolean required;
    private final String key, name, description;
    private final Class<? extends ProjectProperty> valueType;

    public PropertyDefinition(boolean required,
                              String key, String name, String description,
                              Class<? extends ProjectProperty> valueType) {
        this.required = required;
        this.key = key;
        this.name = name;
        this.description = description;
        this.valueType = valueType;
    }

    public boolean isRequired() {
        return required;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Class<? extends ProjectProperty> getValueType() {
        return valueType;
    }

    @Override
    public String toString() {
        return key;
    }
}
