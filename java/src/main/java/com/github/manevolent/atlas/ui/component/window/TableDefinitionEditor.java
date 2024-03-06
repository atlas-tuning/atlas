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
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static com.github.manevolent.atlas.definition.Axis.X;
import static com.github.manevolent.atlas.definition.Axis.Y;
import static com.github.manevolent.atlas.ui.Fonts.bold;
import static com.github.manevolent.atlas.ui.Fonts.getTextColor;

public class TableDefinitionEditor extends Window implements InternalFrameListener {
    private final Table realTable;
    private final Table workingTable;
    private final Map<Axis, JCheckBox> axisCheckboxes = new HashMap<>();

    private JPanel rootPanel;
    private TableEditor preview;
    private boolean dirty = false;

    public TableDefinitionEditor(EditorForm editor, Table table) {
        super(editor);
        this.realTable = table;
        this.workingTable = table.copy();
    }

    public Table getTable() {
        return realTable;
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

    private void createSaveRow(JPanel entryPanel, int row) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        panel.add(Inputs.nofocus(Inputs.button(CarbonIcons.RESET, "Reset", "Reset entered values", () -> {
            if (JOptionPane.showConfirmDialog(getComponent(),
                    "Are you sure you want to reset " +
                    workingTable.getName() + "?",
                    "Reset",
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }

            workingTable.apply(realTable);
            dirty = false;
            Log.ui().log(Level.INFO, "Reset table definition back to project copy.");
            reinitialize();
            updateTitle();
        })));

        panel.add(Inputs.nofocus(Inputs.button(CarbonIcons.SAVE, "Save", "Save entered values", this::save)));

        JButton copy = Inputs.nofocus(Inputs.button(CarbonIcons.COPY, "Copy", "Copy this definition into a new table", () -> {
            String newTableName = workingTable.getName();
            if (newTableName.contains("-")) {
                newTableName = newTableName.substring(0, newTableName.lastIndexOf("-")) + "- Copy";
            } else {
                newTableName = "Copy";
            }
            Table newTable = workingTable.copy();
            newTable.setName(newTableName);
            getParent().openTableDefinition(newTable);
        }));
        panel.add(copy);

        JButton open = Inputs.nofocus(Inputs.button(CarbonIcons.OPEN_PANEL_TOP, "Open", "Open table and edit cells",
                () -> {
                    getParent().openTable(realTable);
                }));
        panel.add(open);

        boolean isProjectTable = getParent().getActiveRom().getTables().contains(realTable);
        if (!isProjectTable) {
            dirty = true;
            updateTitle();
        }
        copy.setEnabled(isProjectTable);
        open.setEnabled(isProjectTable);

        entryPanel.add(panel,
                Layout.gridBagConstraints(
                        GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                        1, row, // pos
                        2, 1, // size
                        1, 1 // weight
                ));
    }

    private void save() {
        realTable.apply(workingTable);

        // Make sure the table is a part of the project
        if (!getParent().getActiveRom().hasTable(realTable)) {
            getParent().getActiveRom().addTable(realTable);
            Log.ui().log(Level.INFO, "Added new table definition of \"" + workingTable.getName()
                    + "\" to project.");
        }

        dirty = false;
        updateTitle();
        Log.ui().log(Level.INFO, "Saved working table definition of \"" + workingTable.getName()
                + "\" to project.");

        // Reload any active editor
        TableEditor editor = getParent().getActiveTableEditor(realTable);
        editor.reload();

        // Reload various menus across the editor
        getParent().updateWindowTitles();
        getParent().getTablesTab().update();
        getParent().getTablesTab().tableOpened(realTable);
    }

    private JPanel createTablePanel() {
        JPanel panel = createEntryPanel();

        panel.add(Layout.emptyBorder(0, 0, 5, 0,
                        Labels.boldText("Table")),
                Layout.gridBagTop(2));

        createEntryRow(
                panel, 1,
                "Name", "The name of this table",
                Inputs.textField(workingTable.getName(), (newName) -> {
                    if (!workingTable.getName().equals(newName)) {
                        workingTable.setName(newName);
                        definitionUpdated();
                    }
                })
        );

        createSaveRow(panel, 2);

        return panel;
    }

    private JPanel createSeriesPanel(Axis axis) {
        Series series;
        if (axis != null) {
            series = workingTable.getSeries(axis);
        } else {
            series = workingTable.getData();
        }

        JPanel panel = createEntryPanel();

        JFlashRegionField flashRegionField = Inputs.flashRegionField(
                getParent().getActiveRom(),
                workingTable, axis, (newRegion, newLength) -> {
            Series s = axis != null ? workingTable.getSeries(axis) : workingTable.getData();
            s.setAddress(newRegion);
            s.setLength(newLength);
            definitionUpdated();
        });

        JTextField nameField = Inputs.textField(series != null ? series.getName() : null, (newName) -> {
            Series s = axis != null ? workingTable.getSeries(axis) : workingTable.getData();
            if (s.getName() == null || !s.getName().equals(newName)) {
                s.setName(newName);
                definitionUpdated();
            }
        });

        JComboBox<Scale> scaleField = Inputs.scaleField(
                getParent().getActiveRom(),
                workingTable, axis,
                "The data scale and format for this series",
                (newScale) -> {
                    Series s = axis != null ? workingTable.getSeries(axis) : workingTable.getData();
                    s.setScale(newScale);
                    if (newScale.getUnit() != null) {
                        s.setUnit(newScale.getUnit());
                    }

                    if (newScale.getFormat() != null) {
                        s.setFormat(newScale.getFormat());
                    }

                    // Shortcut to set the name of a series
                    if (newScale.getName() != null && !newScale.getName().isBlank() &&
                            (nameField.getText() == null || nameField.getText().isBlank())) {
                        nameField.setText(newScale.getName());
                    }
                    definitionUpdated();
                }
        );

        boolean enabled;

        if (axis != null) {
            JCheckBox checkBox = Inputs.checkbox(axis.name() + " axis",
                    workingTable.hasAxis(axis),
                    checked -> {
                        if (checked && !workingTable.hasAxis(axis)) {
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

                            workingTable.setAxis(axis, newSeries);
                            scaleField.setSelectedItem(scale);
                        } else {
                            workingTable.removeAxis(axis);
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
                checkBox.setEnabled(!workingTable.hasAxis(Y));
            } else if (axis == Y) {
                checkBox.setEnabled(workingTable.hasAxis(X));
            }

            checkBox.setFocusable(false);
            axisCheckboxes.put(axis, checkBox);
            panel.add(Layout.emptyBorder(0, 0, 1, 0, bold(checkBox)),
                    Layout.gridBagTop(2)
            );

            enabled = checkBox.isSelected();
        } else {
            enabled = true;
            panel.add(Layout.emptyBorder(0, 0, 1, 0,
                            Labels.boldText("Data series")), Layout.gridBagTop(2));
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
        dirty = true;
        updateTitle();
        updatePreview();
    }

    @Override
    protected void preInitComponent(JInternalFrame window) {
        window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        window.addInternalFrameListener(this);
    }

    @Override
    protected void initComponent(JInternalFrame window) {
        updateTitle();

        if (rootPanel != null) {
            window.remove(rootPanel);
        }

        Color borderColor = Color.GRAY.darker();
        rootPanel = Layout.matteBorder(1, 0, 0, 0, borderColor, new JPanel(new GridBagLayout()));

        JPanel seriesPanel = Layout.matteBorder(0, 0, 1, 0, borderColor, new JPanel());
        seriesPanel.setLayout(new BoxLayout(seriesPanel, BoxLayout.X_AXIS));

        seriesPanel.add(createTablePanel());
        seriesPanel.add(Separators.vertical());
        seriesPanel.add(createTableDataPanel());
        seriesPanel.add(Separators.vertical());
        seriesPanel.add(createSeriesPanel(X));
        seriesPanel.add(Separators.vertical());
        seriesPanel.add(createSeriesPanel(Y));

        rootPanel.add(seriesPanel, Layout.gridBagConstraints(
            GridBagConstraints.NORTHWEST,
            GridBagConstraints.HORIZONTAL,
            0, 0,
            1, 0
        ));

        preview = new TableEditor(getParent(), workingTable, true);
        rootPanel.add(preview.getComponent().getContentPane(),
                Layout.gridBagConstraints(
                        GridBagConstraints.CENTER,
                        GridBagConstraints.BOTH,
                        0, 1,
                        1, 1
                ));

        window.add(rootPanel);
    }

    private Component createTableDataPanel() {
        return createSeriesPanel(null); // null implies table data series
    }

    @Override
    public Icon getIcon() {
        return Icons.get(CarbonIcons.CHART_CUSTOM, getTextColor());
    }

    @Override
    public String getTitle() {
        return "Define Table - " + workingTable.getName() + (dirty ? "*" : "");
    }

    @Override
    public void internalFrameOpened(InternalFrameEvent e) {

    }

    @Override
    public void internalFrameClosing(InternalFrameEvent e) {
        if (dirty) {
            int answer = JOptionPane.showConfirmDialog(getComponent(),
                    "You have unsaved changes to " +
                    workingTable.getName() + " that will be lost. Save before closing?",
                    "Unsaved changes",
                    JOptionPane.YES_NO_CANCEL_OPTION
            );

            switch (answer) {
                case JOptionPane.YES_OPTION:
                    save();
                case JOptionPane.NO_OPTION:
                    getComponent().dispose();
                    break;
                case JOptionPane.CANCEL_OPTION:
                    return;
            }
        } else {
            getComponent().dispose();
        }
    }

    @Override
    public void internalFrameClosed(InternalFrameEvent e) {

    }

    @Override
    public void internalFrameIconified(InternalFrameEvent e) {

    }

    @Override
    public void internalFrameDeiconified(InternalFrameEvent e) {

    }

    @Override
    public void internalFrameActivated(InternalFrameEvent e) {

    }

    @Override
    public void internalFrameDeactivated(InternalFrameEvent e) {

    }
}
