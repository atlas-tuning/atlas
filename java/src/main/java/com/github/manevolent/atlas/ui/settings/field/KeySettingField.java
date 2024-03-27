package com.github.manevolent.atlas.ui.settings.field;

import com.github.manevolent.atlas.model.KeyProperty;
import com.github.manevolent.atlas.model.Project;
import com.github.manevolent.atlas.model.uds.SecurityAccessProperty;
import com.github.manevolent.atlas.ui.component.field.KeyField;

import javax.swing.*;
import java.awt.*;

public class KeySettingField extends AbstractSettingField<SecurityAccessProperty> {
    private final Frame parent;
    private final Project project;
    private final String key;

    private KeyProperty property;
    private boolean dirty;

    public KeySettingField(Frame parent, Project project,
                           String key, String name, String tooltip) {
        super(name, tooltip);

        this.parent = parent;
        this.project = project;
        this.key = key;

        this.property = project.getProperty(key, KeyProperty.class);

        if (property != null) {
            // Clone so applying actually has a purpose
            property = property.clone();
        }
    }

    @Override
    public JComponent getInputComponent() {
        return new KeyField(parent, property, getTooltip(), (newValue) -> {
            property = newValue;
            dirty = true;
        });
    }

    @Override
    public boolean apply() {
        project.addProperty(key, property);
        dirty = false;
        return true;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }
}
