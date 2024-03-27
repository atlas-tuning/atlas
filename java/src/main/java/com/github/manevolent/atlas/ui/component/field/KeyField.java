package com.github.manevolent.atlas.ui.component.field;


import com.github.manevolent.atlas.model.KeyProperty;
import com.github.manevolent.atlas.model.uds.SecurityAccessProperty;
import com.github.manevolent.atlas.ui.dialog.BinaryInputDialog;
import com.github.manevolent.atlas.ui.dialog.KeyDataDialog;
import com.github.manevolent.atlas.ui.dialog.SecurityAccessDialog;
import com.github.manevolent.atlas.ui.util.Fonts;
import com.github.manevolent.atlas.ui.util.Inputs;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class KeyField extends JPanel {
    private JTextField textField;
    private JButton selectButton;

    private KeyProperty property;

    public KeyField(Frame parent,
                    KeyProperty existing,
                    String tooltip,
                    Consumer<KeyProperty> changed) {

        setLayout(new BorderLayout());

        Runnable updateText = () -> {
            String text;
            if (property != null) {
                text = com.github.manevolent.atlas.Frame.toHexString(property.getKey());
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
            KeyProperty newProperty = property == null ? getDefault() : property;
            newProperty = KeyDataDialog.show(parent, newProperty);
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
                if (KeyField.this.isEnabled()) {
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

    public KeyProperty getDefault() {
        return new KeyProperty(new byte[0]);
    }

    public KeyProperty getProperty() {
        return property;
    }
}
