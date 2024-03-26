package com.github.manevolent.atlas.ui.component.field;


import com.github.manevolent.atlas.model.Project;
import com.github.manevolent.atlas.model.uds.SecurityAccessProperty;
import com.github.manevolent.atlas.ui.dialog.MemoryAddressDialog;
import com.github.manevolent.atlas.ui.dialog.SecurityAccessDialog;
import com.github.manevolent.atlas.ui.util.Fonts;
import com.github.manevolent.atlas.ui.util.Inputs;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SecurityAccessField extends JPanel {
    private JTextField textField;
    private JButton selectButton;

    private SecurityAccessProperty property;

    public SecurityAccessField(Frame parent,
                               SecurityAccessProperty existing,
                               String tooltip,
                               Consumer<SecurityAccessProperty> changed) {

        setLayout(new BorderLayout());

        Runnable updateText = () -> {
            String text;
            if (property != null) {
                text = "Level " + property.getLevel() + ": " +
                        com.github.manevolent.atlas.Frame.toHexString(property.getKey());
            } else {
                text = "";
            }

            textField.setText(text);

            SwingUtilities.invokeLater(() -> {
                textField.repaint();
            });
        };

        // Set default values
        property = existing;

        textField = Inputs.textField(
                "",
                tooltip,
                false,
                (newValue) -> { /* ignore */ }
        );

        textField.setFont(Fonts.VALUE_FONT);

        updateText.run();

        Runnable set = () -> {
            SecurityAccessProperty newProperty = property == null ? getDefault() : property;
            newProperty = SecurityAccessDialog.show(parent, newProperty);
            if (newProperty != null) {
                property = newProperty;
                changed.accept(property);
            }
            updateText.run();
        };

        textField.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        textField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SecurityAccessField.this.isEnabled()) {
                    SwingUtilities.invokeLater(set);
                }
            }
        });

        textField.setFocusable(false);

        add(textField, BorderLayout.CENTER);

        selectButton = Inputs.button(
                CarbonIcons.DATA_REFERENCE,
                new JLabel().getForeground(),
                set);

        selectButton.setToolTipText("Change value...");

        selectButton.setFocusable(false);

        add(selectButton, BorderLayout.EAST);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        selectButton.setEnabled(enabled);
    }

    public SecurityAccessProperty getDefault() {
        return new SecurityAccessProperty(1, new byte[16]);
    }

    public SecurityAccessProperty getProperty() {
        return property;
    }
}
