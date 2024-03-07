package com.github.manevolent.atlas.ui.component.tab;

import com.github.manevolent.atlas.definition.*;
import com.github.manevolent.atlas.logging.Log;
import com.github.manevolent.atlas.ui.Icons;
import com.github.manevolent.atlas.ui.Inputs;
import com.github.manevolent.atlas.ui.Labels;
import com.github.manevolent.atlas.ui.Layout;
import com.github.manevolent.atlas.ui.component.toolbar.FormatsTabToolbar;
import com.github.manevolent.atlas.ui.component.toolbar.OperationsToolbar;
import com.github.manevolent.atlas.ui.component.window.TableEditor;
import com.github.manevolent.atlas.ui.component.window.Window;
import com.github.manevolent.atlas.ui.window.EditorForm;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static com.github.manevolent.atlas.ui.Fonts.getTextColor;
import static com.github.manevolent.atlas.ui.Layout.*;

public class FormatsTab extends Tab implements ListSelectionListener {
    private JList<Scale> list;
    private JList<ScalingOperation> ops;
    private JPanel center;

    private final Map<Scale, Scale> workingCopies = new HashMap<>();
    private final Map<Scale, Boolean> dirtyMap = new HashMap<>();

    private JButton saveButton;
    private JPanel informationContent;

    public FormatsTab(EditorForm editor) {
        super(editor);
    }

    private ListModel<ScalingOperation> getOperationsModel() {
        DefaultListModel<ScalingOperation> model = new DefaultListModel<>();
        Scale scale = getSelectedScale();

        scale.getOperations().forEach(model::addElement);

        return model;
    }

    private JList<ScalingOperation> initOperationsList() {
        JList<ScalingOperation> list = new JList<>(getOperationsModel());

        list = Layout.minimumWidth(list, 200);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new Renderer(TableEditor.valueFont));
        return Layout.emptyBorder(list);
    }

    private ListModel<Table> getUsagesListModel() {
        DefaultListModel<Table> model = new DefaultListModel<>();
        Scale scale = list.getSelectedValue();

        if (scale == null) {
            return model;
        }

        getParent().getActiveRom().getTables()
                .stream().filter(table -> table.hasScale(scale))
                .forEach(model::addElement);

        return model;
    }

    private JList<Table> initUsagesList() {
        JList<Table> list = new JList<>(getUsagesListModel());
        list = Layout.minimumWidth(list, 200);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return Layout.emptyBorder(list);
    }

    private ListModel<Scale> getFormatListModel() {
        DefaultListModel<Scale> model = new DefaultListModel<>();

        getParent().getActiveRom().getScales().stream()
                .sorted(Comparator.comparing(Scale::toString))
                .forEach(model::addElement);

        return model;
    }

    private JList<Scale> initFormatList() {
        JList<Scale> list = new JList<>(getFormatListModel());
        list.setCellRenderer(new Renderer(new JLabel().getFont()));
        list = Layout.minimumWidth(list, 200);
        list.addListSelectionListener(this);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return Layout.emptyBorder(list);
    }

    private void addHeader(JPanel panel, Ikon icon, String text) {
        JComponent component = Layout.emptyBorder(0, 0, 5, 0,
                        Layout.alignLeft(Labels.text(icon, text)));

        panel.add(component, BorderLayout.NORTH);
    }


    private JComponent createEntryRow(JPanel entryPanel, int row,
                                      String label, String helpText,
                                      JComponent input) {
        // Label
        JLabel labelField = Labels.darkerText(label);
        entryPanel.add(labelField, Layout.gridBagConstraints(
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, 0, row, 0, 1
        ));

        Insets insets = new JTextField().getInsets();

        // Entry
        if (input instanceof JLabel) {
            input.setBorder(BorderFactory.createEmptyBorder(
                    insets.top,
                    0,
                    insets.bottom,
                    insets.right
            ));
        }
        input.setToolTipText(helpText);
        entryPanel.add(input,
                Layout.gridBagConstraints(GridBagConstraints.NORTHWEST,
                        GridBagConstraints.HORIZONTAL, 1, row, 1, 0));

        labelField.setVerticalAlignment(SwingConstants.TOP);

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
            Scale scale = getSelectedScale();
            if (scale == null) {
                return;
            }

            if (JOptionPane.showConfirmDialog(getComponent(),
                    "Are you sure you want to reset " +
                            scale.getName() + "?",
                    "Reset",
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }

            scale.apply(list.getSelectedValue());
            dirtyMap.put(scale, false);
            saveButton.setEnabled(false);
            Log.ui().log(Level.INFO, "Reset format definition back to project copy.");
            update();
        })));

        saveButton = Inputs.button(CarbonIcons.SAVE, "Save", "Save entered values", this::save);
        saveButton.setEnabled(isDirty());
        panel.add(Inputs.nofocus(saveButton));

        JButton copy = Inputs.nofocus(Inputs.button(CarbonIcons.COPY, "Copy", "Copy this format", () -> {
            Scale scale = getSelectedScale();
            if (scale == null) {
                return;
            }

            String newScaleName = scale.getName() + " (Copy)";
            Scale newScale = scale.copy();
            newScale.setName(newScaleName);
            workingCopies.put(newScale, newScale);
            getParent().getActiveRom().getScales().add(newScale);

            update();
            updateListModel();
        }));
        panel.add(copy);

        boolean isNewTable = workingCopies.containsKey(getSelectedScale());
        if (isNewTable) {
            dirtyMap.put(getSelectedScale(), true);
            operationChanged();
        }

        copy.setEnabled(!isNewTable);

        entryPanel.add(panel,
                Layout.gridBagConstraints(
                        GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
                        1, row, // pos
                        2, 1, // size
                        1, 1 // weight
                ));
    }

    private boolean isDirty() {
        Scale scale = list.getSelectedValue();
        if (scale == null) {
            return false;
        }

        Boolean result = dirtyMap.get(scale);
        if (result == null) {
            return false;
        } else {
            return result;
        }
    }

    private void save() {
        Scale realScale = list.getSelectedValue();
        Scale workingScale = workingCopies.get(realScale);
        if (realScale == null || workingScale == null) {
            return;
        }

        if (realScale == workingScale) {
            // It's a new copy
            getParent().getActiveRom().getScales().add(realScale);
            workingCopies.put(realScale, realScale.copy());
        } else {
            realScale.apply(workingScale);
        }

        dirtyMap.put(realScale, false);
        update();
        updateListModel();

        getParent().getOpenWindows().forEach(Window::reload);
    }

    private JPanel buildSettings() {
        Scale scale = getSelectedScale();
        JPanel settingsPanel = emptyBorder(0, 5, 0, 5, new JPanel(new BorderLayout()));
        addHeader(settingsPanel, CarbonIcons.SETTINGS, "Settings");

        JPanel content = new JPanel(new BorderLayout());
        JPanel inner = new JPanel(new GridBagLayout());
        Layout.topBorder(5, inner);

        JTextField nameField = Inputs.textField(scale.getName(), null, (name) -> {
                    scale.setName(name);
                    operationChanged();
                });
        createEntryRow(inner, 0, "Name", "Name of the format", nameField);

        JComboBox<Unit> unitField = Inputs.unitField(scale.getName(),
                scale.getUnit(), (unit) -> {
                    scale.setUnit(unit);
                    operationChanged();
                });
        createEntryRow(inner, 1, "Unit", null, unitField);

        JComboBox<DataFormat> dataType = Inputs.dataTypeField(scale.getName(),
                scale.getFormat(),  (format) -> {
                    scale.setFormat(format);
                    operationChanged();
                });
        createEntryRow(inner, 2, "Data Type", null, dataType);

        createSaveRow(inner, 3);

        content.add(inner, BorderLayout.CENTER);
        matteBorder(1, 0, 0, 0, Color.GRAY.darker(), content);

        settingsPanel.add(topBorder(5, wrap(new BorderLayout(), content, BorderLayout.NORTH)),
                BorderLayout.CENTER);

        return settingsPanel;
    }

    private JPanel buildOperations() {
        JPanel operationsPanel = emptyBorder(0, 5, 0, 5, new JPanel(new BorderLayout()));
        addHeader(operationsPanel, CarbonIcons.CALCULATOR, "Operations");

        JPanel content = new JPanel(new BorderLayout());
        JPanel inner = new JPanel(new BorderLayout());

        inner.add(new OperationsToolbar(this).getComponent(), BorderLayout.NORTH);
        inner.add(Layout.emptyBorder(scrollVertical(ops = initOperationsList())), BorderLayout.CENTER);

        content.add(matteBorder(1, 1, 1, 1, Color.GRAY.darker(), inner), BorderLayout.CENTER);

        operationsPanel.add(topBorder(5, content), BorderLayout.CENTER);

        return operationsPanel;
    }

    private String formatValue(float value, Unit unit) {
        if (value > 0 && value < 0.01f)
            value = 0.01f;
        return String.format("%.2f%s", value, unit.toString());
    }

    private JPanel buildInformation() {
        Scale scale = getSelectedScale();

        if (informationContent == null) {
            informationContent = emptyBorder(0, 5, 0, 5, new JPanel(new BorderLayout()));
        } else {
            informationContent.removeAll();
        }

        addHeader(informationContent, CarbonIcons.INFORMATION_SQUARE, "Info");

        JPanel content = new JPanel(new GridBagLayout());

        createEntryRow(
                content, 0, "Minimum", "Minimum value for data using this scale",
                Labels.text(formatValue(scale.getMinimum(), scale.getUnit()), Color.RED)
        );

        createEntryRow(
                content, 1, "Maximum", "Maximum value for data using this scale",
                Labels.text(formatValue(scale.getMaximum(), scale.getUnit()), Color.GREEN)
        );

        createEntryRow(
                content, 2, "Precision", "Precision of values for data using this scale",
                Labels.text(formatValue(scale.getPrecision(), scale.getUnit()))
        );

        matteBorder(1, 0, 0, 0, Color.GRAY.darker(), content);

        informationContent.add(topBorder(5, wrap(new BorderLayout(), content, BorderLayout.NORTH)),
            BorderLayout.CENTER);
        return informationContent;
    }

    private JPanel buildUsages() {
        JPanel usagesPanel = emptyBorder(0, 0, 5, 0, new JPanel(new BorderLayout()));
        addHeader(usagesPanel, CarbonIcons.DATA_TABLE, "Usages");

        JScrollPane content = scrollBoth(initUsagesList());
        maximumWidth(content, 250);
        matteBorder(1, 1, 1, 1, Color.GRAY.darker(), content);

        usagesPanel.add(emptyBorder(5, 0, 5, 0, wrap(new BorderLayout(), content, BorderLayout.NORTH)),
                BorderLayout.CENTER);

        return usagesPanel;
    }

    private void buildView(JPanel parent) {
        parent.add(buildInformation());
        parent.add(buildSettings());
        parent.add(buildOperations());
        parent.add(buildUsages());
    }

    private void initView() {
        center.removeAll();
        getComponent().remove(center);

        if (!list.isSelectionEmpty()) {
            center.setLayout(new BoxLayout(center, BoxLayout.X_AXIS));
            buildView(center);
            getComponent().add(emptyBorder(5, 5, 5, 5, center), BorderLayout.CENTER);
        }
    }

    public void updateListModel() {
        Scale selectedFormat = list.getSelectedValue();
        list.setModel(getFormatListModel());
        list.setSelectedValue(selectedFormat, true);
    }

    public void update() {
        if (list.getSelectedValue() != null) {
            workingCopies.computeIfAbsent(list.getSelectedValue(), Scale::copy);
        }

        initView();

        getComponent().revalidate();
        getComponent().repaint();
    }

    @Override
    protected void initComponent(JPanel tab) {
        tab.setLayout(new BorderLayout());

        JPanel left = new JPanel(new BorderLayout());

        left.add(new FormatsTabToolbar(this).getComponent(), BorderLayout.NORTH);
        left.add(Layout.emptyBorder(scrollVertical(list = initFormatList())), BorderLayout.CENTER);

        tab.add(matteBorder(0, 0, 0, 1, Color.GRAY.darker(), left), BorderLayout.WEST);

        center = new JPanel(new BorderLayout());

        // Autoselect the first item in the list
        if (list.isSelectionEmpty()) {
            list.setSelectedIndex(0);
        }
    }

    public Scale getSelectedScale() {
        Scale realCopy = list.getSelectedValue();
        if (realCopy == null) {
            return null;
        }

        return workingCopies.get(realCopy);
    }

    @Override
    public String getTitle() {
        return "Formats";
    }

    @Override
    public Icon getIcon() {
        return Icons.get(CarbonIcons.DATA_VIS_2, getTextColor());
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        update();
    }

    public void updateOperationsList() {
        if (ops == null) {
            return;
        }

        ScalingOperation selected = ops.getSelectedValue();
        ops.setModel(getOperationsModel());
        ops.setSelectedValue(selected, true);
    }

    public void operationChanged() {
        Scale scale = list.getSelectedValue();
        if (scale == null) {
            return;
        }

        dirtyMap.put(scale, true);
        saveButton.setEnabled(true);

        updateOperationsList();
        updateInformation();

        getComponent().revalidate();
        getComponent().repaint();
    }

    private void updateInformation() {
        buildInformation();
    }

    public void deleteOperation() {
        ScalingOperation operation = ops.getSelectedValue();
        if (operation == null) {
            return;
        }

        if (JOptionPane.showConfirmDialog(getComponent(),
                "Are you sure you want to delete \"" + operation + "\"?",
                "Delete operation",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        getSelectedScale().removeOperation(operation);

        updateOperationsList();
    }

    public void addOperation() {

        operationChanged();
    }

    public void moveDown() {
        ScalingOperation operation = ops.getSelectedValue();
        if (operation == null) {
            return;
        }
        getSelectedScale().moveOperationDown(operation);
        operationChanged();
    }

    public void moveUp() {
        ScalingOperation operation = ops.getSelectedValue();
        if (operation == null) {
            return;
        }
        getSelectedScale().moveOperationUp(operation);
        operationChanged();
    }

    public void editOperation() {

        operationChanged();
    }

    private static class Renderer extends DefaultListCellRenderer {
        private final Font font;

        private Renderer(Font font) {
            this.font = font;
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            component.setFont(font);
            return component;
        }
    }
}
