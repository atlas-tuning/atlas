package com.github.manevolent.atlas.ui.component.window;

import com.github.manevolent.atlas.definition.Axis;
import com.github.manevolent.atlas.definition.Scale;
import com.github.manevolent.atlas.definition.Series;
import com.github.manevolent.atlas.definition.Table;
import com.github.manevolent.atlas.logging.Log;
import com.github.manevolent.atlas.ui.*;
import com.github.manevolent.atlas.ui.component.JFlashRegionField;
import com.github.manevolent.atlas.ui.window.EditorForm;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

import static com.github.manevolent.atlas.definition.Axis.X;
import static com.github.manevolent.atlas.definition.Axis.Y;
import static com.github.manevolent.atlas.ui.Fonts.bold;

public class TableDefinitionEditor extends Window {
    private final Table table;
    private TableEditor preview;
    private Map<Axis, JCheckBox> axisCheckboxes = new HashMap<>();

    public TableDefinitionEditor(EditorForm editor, Table table) {
        super(editor);
        this.table = table;
    }

    public Table getTable() {
        return table;
    }

    private JPanel createEntryPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return panel;
    }

    private JComponent createEntryRow(JPanel entryPanel, int row,
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
                insets.left,
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

    private JComponent createEmptyRow(JPanel entryPanel, int row) {
        JLabel empty = new JLabel();
        empty.setText("Help");
        entryPanel.add(empty, Layout.gridBagConstraints(
                GridBagConstraints.WEST, GridBagConstraints.NONE, 0, row, 1, 1
        ));
        return empty;
    }

    private JPanel createTablePanel() {
        JPanel panel = createEntryPanel();

        panel.add(Labels.boldText("Table"), Layout.gridBagTop());

        createEntryRow(
                panel, 1,
                "Name", "The name of this table",
                Inputs.textField(table.getName(), (newName) -> {
                    if (!table.getName().equals(newName)) {
                        table.setName(newName);
                        definitionUpdated();
                    }
                })
        );

        return panel;
    }

    private JPanel createSeriesPanel(Axis axis) {
        Series series;
        if (axis != null) {
            series = table.getSeries(axis);
        } else {
            series = table.getData();
        }

        JPanel panel = createEntryPanel();

        JFlashRegionField flashRegionField = Inputs.flashRegionField(
                getParent().getActiveRom(),
                table, axis, (newRegion, newLength) -> {
            Series s = axis != null ? table.getSeries(axis) : table.getData();
            s.setAddress(newRegion);
            s.setLength(newLength);
            definitionUpdated();
        });

        JComboBox<Scale> scaleField = Inputs.scaleField(
                getParent().getActiveRom(),
                table, axis,
                "The data scale and format for this series",
                (newScale) -> {
                    Series s = axis != null ? table.getSeries(axis) : table.getData();
                    s.setScale(newScale);
                    if (newScale.getUnit() != null) {
                        s.setUnit(newScale.getUnit());
                    }
                    if (newScale.getFormat() != null) {
                        s.setFormat(newScale.getFormat());
                    }
                    definitionUpdated();
                }
        );

        JTextField nameField = Inputs.textField(series != null ? series.getName() : null, (newName) -> {
            Series s = axis != null ? table.getSeries(axis) : table.getData();
            if (s.getName() == null || !s.getName().equals(newName)) {
                s.setName(newName);
                definitionUpdated();
            }
        });

        boolean enabled;

        if (axis != null) {
            JCheckBox checkBox = Inputs.checkbox(axis.name() + " axis",
                    table.hasAxis(axis),
                    checked -> {
                        if (checked && !table.hasAxis(axis)) {
                            // Try to automatically pick a scale if one isn't picked
                            Scale scale = (Scale) scaleField.getSelectedItem();
                            if (scale == null) {
                                scale = (Scale) scaleField.getItemAt(0);
                            }

                            Series newSeries = Series.builder()
                                    .withName(nameField.getText())
                                    .withScale(scale)
                                    .withAddress(flashRegionField.getDataAddress())
                                    .withLength(flashRegionField.getDataLength())
                                    .build();

                            table.setAxis(axis, newSeries);
                            scaleField.setSelectedItem(scale);
                        } else {
                            table.removeAxis(axis);
                        }

                        if (axis == Y) {
                            axisCheckboxes.get(X).setEnabled(!checked);
                        } else if (axis == X) {
                            axisCheckboxes.get(Y).setEnabled(checked);
                        }

                        nameField.setEnabled(checked);
                        flashRegionField.setEnabled(checked);
                        scaleField.setEnabled(checked);

                        definitionUpdated();
                    });

            if (axis == X) {
                checkBox.setEnabled(!table.hasAxis(Y));
            } else if (axis == Y) {
                checkBox.setEnabled(table.hasAxis(X));
            }

            checkBox.setFocusable(false);
            axisCheckboxes.put(axis, checkBox);
            panel.add(bold(checkBox),
                    Layout.gridBagTop()
            );

            enabled = checkBox.isSelected();
        } else {
            enabled = true;
            panel.add(Labels.boldText("Data series"), Layout.gridBagTop());
        }

        nameField.setEnabled(enabled);
        flashRegionField.setEnabled(enabled);
        scaleField.setEnabled(enabled);

        createEntryRow(panel, 1,
                "Name", axis != null ? "The name of this axis" : "The name of this series",
                nameField);

        createEntryRow(panel, 2,
                axis != null ? "Region" : "Address",
                axis != null ? "The data region for this axis" : "The data address for this series",
                flashRegionField);

        createEntryRow(panel, 3,
                "Format",
                axis != null ? "The format of the data in this axis" : "The format of the data in this series",
                scaleField);

        return panel;
    }

    private void updatePreview() {
        if (preview == null) {
            return;
        }

        preview.reload();
    }

    private void definitionUpdated() {
        getComponent().setTitle(getTitle());
        updatePreview();
    }

    @Override
    protected void initComponent(JInternalFrame window) {
        Color borderColor = Color.GRAY.darker();
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, borderColor));

        JPanel seriesPanel = new JPanel();
        seriesPanel.setLayout(new BoxLayout(seriesPanel, BoxLayout.X_AXIS));

        seriesPanel.setBorder(BorderFactory.createMatteBorder(
                0, 0, 1, 0, borderColor));

        seriesPanel.add(createTablePanel());
        seriesPanel.add(Separators.vertical());
        seriesPanel.add(createTableDataPanel());
        seriesPanel.add(Separators.vertical());
        seriesPanel.add(createSeriesPanel(X));
        seriesPanel.add(Separators.vertical());
        seriesPanel.add(createSeriesPanel(Y));

        panel.add(seriesPanel, Layout.gridBagConstraints(
            GridBagConstraints.NORTHWEST,
            GridBagConstraints.HORIZONTAL,
            0, 0,
            1, 0
        ));

        preview = new TableEditor(getParent(), table, true);
        panel.add(preview.getComponent().getContentPane(),
                Layout.gridBagConstraints(
                        GridBagConstraints.CENTER,
                        GridBagConstraints.BOTH,
                        0, 1,
                        1, 1
                ));

        window.add(panel);
    }

    private Component createTableDataPanel() {
        return createSeriesPanel(null); // null implies table data series
    }

    @Override
    public Icon getIcon() {
        return Icons.get(CarbonIcons.CHART_CUSTOM, Color.WHITE);
    }

    @Override
    public String getTitle() {
        if (table.getName() == null) {
            return "Define New Table";
        } else {
            return "Define Table - " + table.getName();
        }
    }
}
