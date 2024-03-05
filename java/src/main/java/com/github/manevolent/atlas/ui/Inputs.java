package com.github.manevolent.atlas.ui;

import com.github.manevolent.atlas.definition.*;
import com.github.manevolent.atlas.ui.component.JFlashRegionField;
import org.kordamp.ikonli.Ikon;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static java.awt.event.ItemEvent.DESELECTED;
import static java.awt.event.ItemEvent.SELECTED;

public class Inputs {

    public static JButton button(Ikon icon, Runnable clicked) {
        JButton button = new JButton(Icons.get(icon));
        button.addActionListener(e -> clicked.run());
        return button;
    }

    public static JButton button(Ikon icon, Color color, Runnable clicked) {
        JButton button = new JButton(Icons.get(icon, color));
        button.addActionListener(e -> clicked.run());
        return button;
    }

    public static JButton button(String title, Runnable clicked) {
        JButton button = new JButton(title);
        button.addActionListener(e -> clicked.run());
        return button;
    }

    public static JCheckBox checkbox(String title, boolean initial,
                                     Consumer<Boolean> changed) {
        JCheckBox checkBox = new JCheckBox(title);
        checkBox.setSelected(initial);
        checkBox.addItemListener(e -> {
            if (e.getStateChange() != SELECTED && e.getStateChange() != DESELECTED) {
                return;
            }
            changed.accept(checkBox.isSelected());
        });
        return checkBox;
    }

    public static JTextField textField(String defaultValue, String toolTip,
                                       boolean editable, Consumer<String> changed) {
        JTextField textField = new JTextField();
        textField.setEditable(editable);

        if (defaultValue != null) {
            textField.setText(defaultValue);
        }

        if (toolTip != null) {
            textField.setToolTipText(toolTip);
        }

        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                changed.accept(textField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changed.accept(textField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                changed.accept(textField.getText());
            }
        });

        return textField;
    }

    public static JTextField textField(String defaultValue, String toolTip, Consumer<String> changed) {
        return textField(defaultValue, toolTip, true, changed);
    }

    public static JTextField textField(String defaultValue, Consumer<String> changed) {
        return textField(defaultValue, null, changed);
    }

    public static JTextField textField(Consumer<String> changed) {
        return textField(null, changed);
    }

    public static JFlashRegionField flashRegionField(Rom rom, Table table, Axis axis,
                                              BiConsumer<FlashAddress, Integer> changed) {
        return new JFlashRegionField(rom ,table, axis, changed);
    }

    public static JComboBox<Scale> scaleField(Rom rom, Table table, Axis axis,
                                              String toolTip, Consumer<Scale> valueChanged) {
        JComboBox<Scale> comboBox = new JComboBox<>(rom.getScales().toArray(new Scale[0]));
        Scale axisScale = table.hasAxis(axis) ? table.getSeries(axis).getScale() : null;
        Scale intended = axis == null ? table.getData().getScale() : axisScale;
        comboBox.setSelectedItem(intended);
        comboBox.addItemListener(e -> {
            if (e.getStateChange() != SELECTED) {
                return;
            }
            valueChanged.accept((Scale)e.getItem());
        });
        return comboBox;
    }
}
