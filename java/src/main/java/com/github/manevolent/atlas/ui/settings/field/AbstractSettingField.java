package com.github.manevolent.atlas.ui.settings.field;

public abstract class AbstractSettingField<V> implements SettingField<V> {
    private final String name, tooltip;

    protected AbstractSettingField(String name, String tooltip) {
        this.name = name;
        this.tooltip = tooltip;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTooltip() {
        return tooltip;
    }
}
