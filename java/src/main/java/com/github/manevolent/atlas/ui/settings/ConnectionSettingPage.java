package com.github.manevolent.atlas.ui.settings;

import com.github.manevolent.atlas.model.PropertyDefinition;
import com.github.manevolent.atlas.connection.ConnectionType;
import com.github.manevolent.atlas.model.Project;
import com.github.manevolent.atlas.ui.settings.field.EnumSettingField;
import com.github.manevolent.atlas.ui.settings.field.SettingField;
import com.github.manevolent.atlas.ui.util.Job;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ConnectionSettingPage extends BasicSettingPage {
    private final Project project;
    private final Frame parent;

    private ConnectionType connectionType;

    public ConnectionSettingPage(Frame parent, Project project) {
        super(parent, CarbonIcons.PLUG_FILLED, "Connection");

        this.parent = parent;
        this.project = project;
        this.connectionType = project.getConnectionType();
    }

    @Override
    protected List<SettingField<?>> createFields() {
        List<SettingField<?>> elements = new ArrayList<>();

        elements.add(new EnumSettingField<>(
                "Connection Type",
                "The communication type for connections to this vehicle",
                ConnectionType.class,
                connectionType,
                t -> {
                    ConnectionType existing = project.getConnectionType();
                    if (t != existing) {
                        if (existing != null && JOptionPane.showConfirmDialog(null,
                                "Are you sure you want to change the project connection type?\r\n" +
                                        "Doing so will interrupt any established connections.",
                                "Warning",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                            return false;
                        }

                        Job.fork(() -> project.setConnectionType(t));
                    }

                    return true;
                },
                (connectionType) -> {
                    this.connectionType = connectionType;
                    reinitialize();
                }));

        // Based on the connection type, make sure to list any settings for this type
        for (PropertyDefinition parameter : connectionType.getFactory().getPropertyDefinitions()) {
            elements.add(createSettingField(parameter, project));
        }

        return elements;
    }

    @Override
    public boolean validate() {
        return true;
    }
}
