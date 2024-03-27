package com.github.manevolent.atlas.ui.dialog.settings;

import com.github.manevolent.atlas.ui.dialog.settings.field.SettingField;
import org.kordamp.ikonli.Ikon;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class DefaultSettingPage extends BasicSettingPage {
    private final List<SettingField<?>> elements;

    public DefaultSettingPage(Frame parent, Ikon icon, String name, List<SettingField<?>> elements) {
        super(parent, icon, name);

        this.elements = elements;
    }

    public DefaultSettingPage(Frame parent, Ikon icon, String name, SettingField<?>... elements) {
        this(parent, icon, name, Arrays.asList(elements));
    }

    @Override
    protected List<SettingField<?>> createFields() {
        return elements;
    }

    @Override
    public boolean validate() {
        return true;
    }
}
