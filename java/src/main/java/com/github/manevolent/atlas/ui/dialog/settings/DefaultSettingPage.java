package com.github.manevolent.atlas.ui.dialog.settings;

import com.github.manevolent.atlas.ui.dialog.settings.element.SettingField;
import com.github.manevolent.atlas.ui.util.Fonts;
import com.github.manevolent.atlas.ui.util.Inputs;
import com.github.manevolent.atlas.ui.util.Labels;
import com.github.manevolent.atlas.ui.util.Layout;
import org.kordamp.ikonli.Ikon;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class DefaultSettingPage extends BasicSettingPage {
    private final List<SettingField<?>> elements;

    public DefaultSettingPage(Ikon icon, String name, List<SettingField<?>> elements) {
        super(icon, name);

        this.elements = elements;
    }

    public DefaultSettingPage(Ikon icon, String name, SettingField<?>... elements) {
        this(icon, name, Arrays.asList(elements));
    }

    @Override
    protected List<SettingField<?>> createFields() {
        return elements;
    }
}
