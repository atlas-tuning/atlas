package com.github.manevolent.atlas.ui.dialog.settings.element;

import com.github.manevolent.atlas.model.Project;
import com.github.manevolent.atlas.model.uds.SecurityAccessProperty;
import com.github.manevolent.atlas.ui.component.field.SecurityAccessField;

import javax.swing.*;
import java.awt.*;

public class SecurityAccessSettingField extends AbstractSettingField<SecurityAccessProperty> {
    private final Frame parent;
    private final Project project;
    private final String key;

    private SecurityAccessProperty property;

    public SecurityAccessSettingField(Frame parent, Project project,
                                      String key, String name, String tooltip) {
        super(name, tooltip);

        this.parent = parent;
        this.project = project;
        this.key = key;

        this.property = project.getProperty(key, SecurityAccessProperty.class);

        if (property != null) {
            // Clone so applying actually has a purpose
            property = property.clone();
        }
    }

    @Override
    public JComponent getInputComponent() {
        return new SecurityAccessField(parent, property, getTooltip(),
                (newValue) -> property = newValue);
    }

    @Override
    public boolean apply() {
        project.addProperty(key, property);
        return true;
    }
}
