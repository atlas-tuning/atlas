package com.github.manevolent.atlas.ui.dialog.settings;

import com.github.manevolent.atlas.connection.ConnectionParameter;
import com.github.manevolent.atlas.connection.ConnectionType;
import com.github.manevolent.atlas.model.Project;
import com.github.manevolent.atlas.model.uds.SecurityAccessProperty;
import com.github.manevolent.atlas.ui.dialog.settings.element.ConnectionTypeSettingField;
import com.github.manevolent.atlas.ui.dialog.settings.element.SecurityAccessSettingField;
import com.github.manevolent.atlas.ui.dialog.settings.element.SettingField;
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
        super(CarbonIcons.PLUG_FILLED, "Connection");

        this.parent = parent;
        this.project = project;
        this.connectionType = project.getConnectionType();
    }

    @Override
    protected List<SettingField<?>> createFields() {
        List<SettingField<?>> elements = new ArrayList<>();

        elements.add(new ConnectionTypeSettingField(
                "Connection Type",
                "The communication type for connections to this vehicle",
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
        for (ConnectionParameter parameter : connectionType.getFactory().getParameters()) {
            SettingField<?> field;

            if (parameter.getValueType().equals(SecurityAccessProperty.class)) {
                field = new SecurityAccessSettingField(parent, project,
                        parameter.getKey(), parameter.getName(), parameter.getDescription());
            } else {
                throw new UnsupportedOperationException(parameter.getValueType().getName());
            }

            elements.add(field);
        }

        return elements;
    }
}
