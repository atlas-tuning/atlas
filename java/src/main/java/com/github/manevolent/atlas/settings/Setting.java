package com.github.manevolent.atlas.settings;

public final class Setting<T extends SettingValue<?>> {
    public static final Setting<StringValue> LAST_OPENED_PROJECT = new Setting<>(StringValue.class, "editor.project.last_opened_file");

    private final String name;
    private final Class<T> valueClass;

    public <C extends Class<T>> Setting(C valueClass, String name) {
        this.name = name;
        this.valueClass = valueClass;
    }

    public String getName() {
        return name;
    }

    public T getValueClass() {
        try {
            return valueClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
