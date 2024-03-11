package com.github.manevolent.atlas.ui.util;

import com.github.manevolent.atlas.model.*;
import com.github.manevolent.atlas.ui.component.MemoryAddressField;
import org.kordamp.ikonli.Ikon;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static java.awt.event.ItemEvent.DESELECTED;
import static java.awt.event.ItemEvent.SELECTED;

public class Inputs {

    public static JButton button(Ikon icon, Runnable clicked) {
        JButton button = new JButton(Icons.get(icon, Fonts.getTextColor()));
        button.addActionListener(e -> clicked.run());
        return button;
    }

    public static JButton button(Ikon icon, java.awt.Color color, Runnable clicked) {
        JButton button = new JButton(Icons.get(icon, color));
        button.addActionListener(e -> clicked.run());
        return button;
    }

    public static JButton button(String title, Runnable clicked) {
        JButton button = new JButton(title);
        button.addActionListener(e -> clicked.run());
        return button;
    }

    public static JButton button(Ikon icon, String title, Runnable clicked) {
        JButton button = new JButton(title, Icons.get(icon, new JLabel().getForeground()));
        button.addActionListener(e -> clicked.run());
        return button;
    }

    public static JButton button(Ikon icon, String title, String toolTipText, Runnable clicked) {
        JButton button = new JButton(title, Icons.get(icon, new JLabel().getForeground()));
        if (toolTipText != null) {
            button.setToolTipText(toolTipText);
        }
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


    private static JTextField textField(String defaultValue, String toolTip,
                                        Font font, boolean editable, Consumer<String> changed) {

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

    public static JTextField textField(String defaultValue, String toolTip,
                                       boolean editable, Consumer<String> changed) {
        return textField(defaultValue, toolTip, (Font) null, editable, changed);
    }

    public static JTextField textField(String defaultValue, String toolTip, Consumer<String> changed) {
        return textField(defaultValue, toolTip, true, changed);
    }

    public static JTextField textField(String defaultValue, Font font, Consumer<String> changed) {
        return textField(defaultValue, (String) null, font, true, changed);
    }

    public static JTextField textField(String defaultValue, Consumer<String> changed) {
        return textField(defaultValue, (String) null, changed);
    }

    public static JTextField textField(Consumer<String> changed) {
        return textField(null, changed);
    }

    public static MemoryAddressField memoryAddressField(Project project, MemoryAddress existing, boolean localOnly,
                                                        Consumer<MemoryAddress> changed) {
        return new MemoryAddressField(project, existing, localOnly, changed);
    }

    public static JComboBox<MemorySection> memorySectionField(List<MemorySection> sections,
                                                              MemorySection value,
                                                              Consumer<MemorySection> changed) {
        JComboBox<MemorySection> comboBox = new JComboBox<>(sections.toArray(new MemorySection[0]));
        MemorySection intended = value == null ? sections.getFirst() : value;
        comboBox.setSelectedItem(intended);
        comboBox.addItemListener(e -> {
            if (e.getStateChange() != SELECTED) {
                return;
            }

            changed.accept((MemorySection)e.getItem());
        });
        return comboBox;
    }

    public static JComboBox<MemorySection> memorySectionField(Project project, MemorySection value,
                                                              Predicate<MemorySection> predicate,
                                                              Consumer<MemorySection> changed) {
        return memorySectionField(
                project.getSections().stream().filter(predicate).toList(),
                value,
                changed);
    }

    public static JSpinner memoryLengthField(Series series, Consumer<Integer> valueChanged) {
        SpinnerNumberModel model = new SpinnerNumberModel(
                series != null ? series.getLength() : 1,
                1, 1_024_000, 1);
        JSpinner spinner = new JSpinner(model);
        ((JSpinner.DefaultEditor)spinner.getEditor()).getTextField().setHorizontalAlignment(JTextField.LEFT);
        spinner.addChangeListener(e -> valueChanged.accept((int) spinner.getValue()));
        return spinner;
    }

    public static JComboBox<Scale> scaleField(Project project, Scale existing,
                                              String toolTip, Consumer<Scale> valueChanged) {
        JComboBox<Scale> comboBox = new JComboBox<>(project.getScales().toArray(new Scale[0]));
        comboBox.setSelectedItem(existing);
        comboBox.addItemListener(e -> {
            if (e.getStateChange() != SELECTED) {
                return;
            }
            valueChanged.accept((Scale)e.getItem());
        });
        return comboBox;
    }

    public static JComboBox<DataFormat> dataTypeField(String toolTip, DataFormat intended,
                                                Consumer<DataFormat> valueChanged) {
        JComboBox<DataFormat> comboBox = new JComboBox<>(DataFormat.values());
        if (intended != null) {
            comboBox.setSelectedItem(intended);
        } else {
            comboBox.setSelectedItem(DataFormat.UBYTE);
        }
        comboBox.addItemListener(e -> {
            if (e.getStateChange() != SELECTED) {
                return;
            }
            valueChanged.accept((DataFormat) e.getItem());
        });
        return comboBox;
    }

    public static JComboBox<Unit> unitField(String toolTip, Unit intended, Consumer<Unit> valueChanged) {
        JComboBox<Unit> comboBox = new JComboBox<>(Unit.values());
        if (intended != null) {
            comboBox.setSelectedItem(intended);
        } else {
            comboBox.setSelectedItem(Unit.RPM);
        }
        comboBox.addItemListener(e -> {
            if (e.getStateChange() != SELECTED) {
                return;
            }
            valueChanged.accept((Unit) e.getItem());
        });
        return comboBox;
    }

    public static JComboBox<ArithmeticOperation> arithmeticOperationField(String toolTip,
                                                                          ScalingOperation operation,
                                                                          Consumer<ArithmeticOperation> valueChanged) {
        JComboBox<ArithmeticOperation> comboBox = new JComboBox<>(ArithmeticOperation.values());
        if (operation != null) {
            comboBox.setSelectedItem(operation.getOperation());
        } else {
            comboBox.setSelectedItem(ArithmeticOperation.ADD);
        }
        comboBox.addItemListener(e -> {
            if (e.getStateChange() != SELECTED) {
                return;
            }
            valueChanged.accept((ArithmeticOperation) e.getItem());
        });
        return comboBox;
    }

    public static <T extends JComponent> T nofocus(T component) {
        component.setFocusable(false);
        return component;
    }

    public static <T extends JComponent> T bg(java.awt.Color color, T component) {
        component.setOpaque(true);
        component.setBackground(color);
        return component;
    }

    public static JPanel createButtonRow(JPanel entryPanel, int row, JButton... buttons) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        Arrays.stream(buttons).forEach(panel::add);

        entryPanel.add(panel,
                Layout.gridBagConstraints(
                        GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                        1, row, // pos
                        2, 1, // size
                        1, 1 // weight
                ));

        return panel;
    }

    public static JPanel createEntryPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return panel;
    }

    public static JComponent createEntryRow(JPanel entryPanel, int row,
                                            String label, String helpText,
                                            JComponent input) {
        // Label
        JLabel labelField = Labels.darkerText(label);
        entryPanel.add(labelField, Layout.gridBagConstraints(
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, 0, row, 0, 1
        ));

        // Entry
        input.setToolTipText(helpText);
        entryPanel.add(input,
                Layout.gridBagConstraints(GridBagConstraints.NORTHWEST,
                        GridBagConstraints.HORIZONTAL, 1, row, 1, 1));

        labelField.setVerticalAlignment(SwingConstants.TOP);

        Insets insets = new JTextField().getInsets();
        labelField.setBorder(BorderFactory.createEmptyBorder(
                insets.top,
                0,
                insets.bottom,
                insets.right
        ));
        labelField.setMaximumSize(new Dimension(
                Integer.MAX_VALUE,
                (int) input.getSize().getHeight()
        ));

        int height = Math.max(input.getHeight(), input.getPreferredSize().height);

        input.setPreferredSize(new Dimension(
                100,
                height
        ));
        input.setSize(
                100,
                height
        );

        return entryPanel;
    }

    public static void bind(Component component, String key, Runnable action, KeyStroke... strokes) {
        Arrays.asList(strokes).forEach(stroke -> {
            ((JComponent)component).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(stroke, action);
        });
        ((JComponent)component).getActionMap().put(action, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }
}
