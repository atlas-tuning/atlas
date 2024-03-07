package com.github.manevolent.atlas.ui.component.window;

import com.github.manevolent.atlas.model.Axis;
import com.github.manevolent.atlas.model.Scale;
import com.github.manevolent.atlas.model.Series;
import com.github.manevolent.atlas.model.Table;
import com.github.manevolent.atlas.logging.Log;
import com.github.manevolent.atlas.ui.*;
import com.github.manevolent.atlas.ui.component.MemoryAddressField;
import com.github.manevolent.atlas.ui.window.EditorForm;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static com.github.manevolent.atlas.model.Axis.X;
import static com.github.manevolent.atlas.model.Axis.Y;
import static com.github.manevolent.atlas.ui.Fonts.bold;
import static com.github.manevolent.atlas.ui.Fonts.getTextColor;

public class TableDefinitionEditor extends Window implements InternalFrameListener {
    private final Table realTable;
    private final Table workingTable;
    private final Map<Axis, JCheckBox> axisCheckboxes = new HashMap<>();

    private JPanel rootPanel;
    private TableEditor preview;
    private boolean dirty = false;

    private JButton save, copy, reset;

    public TableDefinitionEditor(EditorForm editor, Table table) {
        super(editor);
        this.realTable = table;
        this.workingTable = table.copy();
    }

    public Table getTable() {
        return realTable;
    }

    private void setDirty(boolean dirty) {
        copy.setEnabled(!dirty);
        save.setEnabled(dirty);
        reset.setEnabled(dirty);
        this.dirty = dirty;
    }

    private void createSaveRow(JPanel entryPanel, int row) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        panel.add(reset = Inputs.nofocus(Inputs.button(CarbonIcons.RESET, "Reset", "Reset entered values", () -> {
            if (JOptionPane.showConfirmDialog(getComponent(),
                    "Are you sure you want to reset " +
                    workingTable.getName() + "?",
                    "Reset",
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }

            workingTable.apply(realTable);
            setDirty(false);
            Log.ui().log(Level.INFO, "Reset table definition back to project copy.");
            reinitialize();
            updateTitle();
        })));

        panel.add(save = Inputs.nofocus(Inputs.button(CarbonIcons.SAVE, "Save", "Save entered values", this::save)));

        panel.add(copy = Inputs.nofocus(Inputs.button(CarbonIcons.COPY, "Copy", "Copy this definition into a new table", () -> {
            String newTableName = JOptionPane.showInputDialog(getParent(), "Specify a name",
                    workingTable.getName() + " (Copy)");
            if (newTableName == null || newTableName.isBlank()) {
                return;
            }
            Table newTable = workingTable.copy();
            newTable.setName(newTableName);
            getParent().openTableDefinition(newTable);
        })));

        JButton open = Inputs.nofocus(Inputs.button(CarbonIcons.OPEN_PANEL_TOP, "Open", "Open table and edit cells",
                () -> {
                    getParent().openTable(realTable);
                }));
        panel.add(open);

        boolean isProjectTable = getParent().getActiveRom().getTables().contains(realTable);
        if (!isProjectTable) {
            setDirty(true);
            updateTitle();
        }

        save.setEnabled(dirty);
        reset.setEnabled(dirty);
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

        setDirty(false);
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
        JPanel panel = Inputs.createEntryPanel();

        panel.add(Layout.emptyBorder(0, 0, 5, 0,
                        Labels.boldText("Table")),
                Layout.gridBagTop(2));

        Inputs.createEntryRow(
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

        JPanel panel = Inputs.createEntryPanel();

        MemoryAddressField memoryAddressField = Inputs.memoryAddressField(
                getParent().getActiveRom(),
                workingTable, axis, true, (newAddress) -> {
            Series s = axis != null ? workingTable.getSeries(axis) : workingTable.getData();
            s.setAddress(newAddress);
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

                    // Shortcut to set the name of a series
                    if (newScale.getName() != null && !newScale.getName().isBlank() &&
                            (nameField.getText() == null || nameField.getText().isBlank())) {
                        nameField.setText(newScale.getName());
                    }

                    definitionUpdated();
                }
        );

        boolean enabled;
        JSpinner memoryLengthField;
        if (axis != null) {
            memoryLengthField = Inputs.memoryLengthField(
                    series,
                    (newLength) -> {
                        Series currentSeries = workingTable.getSeries(axis);
                        if (currentSeries != null) {
                            currentSeries.setLength(newLength);
                            workingTable.updateLength();
                            definitionUpdated();
                        }
                    }
            );

            JCheckBox checkBox = Inputs.checkbox(axis.name() + " axis",
                    workingTable.hasAxis(axis),
                    checked -> {
                        if (checked && !workingTable.hasAxis(axis)) {
                            // Try to automatically pick a scale if one isn't picked
                            Scale scale = (Scale) scaleField.getSelectedItem();
                            if (scale == null) {
                                scale = scaleField.getItemAt(0);
                            }

                            Series newSeries = Series.builder()
                                    .withName(nameField.getText())
                                    .withScale(scale)
                                    .withAddress(memoryAddressField.getDataAddress())
                                    .withLength((int) memoryLengthField.getValue())
                                    .build();

                            workingTable.setAxis(axis, newSeries);
                            scaleField.setSelectedItem(scale);
                        } else {
                            workingTable.removeAxis(axis);
                        }

                        workingTable.updateLength();

                        if (axis == Y) {
                            axisCheckboxes.get(X).setEnabled(!checked);
                        } else if (axis == X) {
                            axisCheckboxes.get(Y).setEnabled(checked);
                        }

                        nameField.setEnabled(checked);
                        memoryAddressField.setEnabled(checked);
                        scaleField.setEnabled(checked);
                        memoryLengthField.setEnabled(checked);

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
            memoryLengthField = null;
            enabled = true;
            panel.add(Layout.emptyBorder(0, 0, 1, 0, Labels.boldText("Data series")),
                    Layout.gridBagTop(2));
        }

        nameField.setEnabled(enabled);
        memoryAddressField.setEnabled(enabled);
        scaleField.setEnabled(enabled);
        if (memoryLengthField != null) {
            memoryLengthField.setEnabled(enabled);
        }

        Inputs.createEntryRow(panel, 1,
                "Name", axis != null ? "The name of this axis" : "The name of this series",
                nameField);

        Inputs.createEntryRow(panel, 2,
                axis != null ? "Region" : "Address",
                axis != null ? "The data region for this axis" : "The data address for this series",
                memoryAddressField);

        Inputs.createEntryRow(panel, 3,
                "Format",
                axis != null ? "The format of the data in this axis" : "The format of the data in this series",
                scaleField);

        if (memoryLengthField != null) {
            Inputs.createEntryRow(panel, 4,
                    "Length",
                    "The length of this axis",
                    memoryLengthField);
        } else {
            //TODO calculate length
        }

        return panel;
    }

    private void updatePreview() {
        if (preview == null) {
            return;
        }

        preview.reload();
    }

    private void definitionUpdated() {
        setDirty(true);
        updateTitle();

        try {
            updatePreview();
        } catch (Exception ex) {
            Log.ui().log(Level.WARNING, "Problem updating table preview for table \"" +
                    getTable().getName() + "\"", ex);
        }
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
    public void reload() {
        reinitialize();
        updatePreview();
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
