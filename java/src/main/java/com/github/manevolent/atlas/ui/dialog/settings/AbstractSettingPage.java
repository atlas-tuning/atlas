package com.github.manevolent.atlas.ui.dialog.settings;

import org.kordamp.ikonli.Ikon;

public abstract class AbstractSettingPage implements SettingPage {
    private final Ikon icon;
    private final String name;

    protected AbstractSettingPage(Ikon icon, String name) {
        this.icon = icon;
        this.name = name;
    }

    @Override
    public Ikon getIcon() {
        return icon;
    }

    @Override
    public String getName() {
        return name;
    }
}
