package com.github.manevolent.atlas.ui.component.tab;

import com.github.manevolent.atlas.logging.Log;
import com.github.manevolent.atlas.model.*;
import com.github.manevolent.atlas.ui.*;
import com.github.manevolent.atlas.ui.component.ColorField;
import com.github.manevolent.atlas.ui.component.MemoryAddressField;
import com.github.manevolent.atlas.ui.component.toolbar.ParametersTabToolbar;
import com.github.manevolent.atlas.ui.component.window.Window;
import com.github.manevolent.atlas.ui.Editor;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.manevolent.atlas.ui.Fonts.getTextColor;
import static com.github.manevolent.atlas.ui.Layout.*;
import static javax.swing.JOptionPane.QUESTION_MESSAGE;

public class ParametersTab extends Tab implements ListSelectionListener {
    private JList<MemoryParameter> list;
    private JPanel center;

    private final Map<MemoryParameter, MemoryParameter> workingCopies = new HashMap<>();
    private final Map<MemoryParameter, Boolean> dirtyMap = new HashMap<>();

    private JButton resetButton;
    private JButton saveButton;
    private JPanel informationContent;

    public ParametersTab(Editor editor, JTabbedPane tabbedPane) {
        super(editor, tabbedPane);
    }

    private ListModel<MemoryParameter> getParameterListModel() {
        DefaultListModel<MemoryParameter> model = new DefaultListModel<>();

        Stream.concat(workingCopies.keySet().stream(),
                        getParent().getActiveRom().getParameters().stream())
                .collect(Collectors.toSet())
                .stream()
                .sorted(Comparator.comparing(MemoryParameter::toString))
                .forEach(model::addElement);

        return model;
    }

    private JList<MemoryParameter> initParameterList() {
        JList<MemoryParameter> list = new JList<>(getParameterListModel());
        list.setCellRenderer(new Renderer(new JLabel().getFont()));
        list = Layout.minimumWidth(list, 200);
        list.addListSelectionListener(this);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return Layout.emptyBorder(list);
    }

    private void addHeader(JPanel panel, Ikon icon, String text) {
        JComponent component = Layout.emptyBorder(0, 0, 5, 0,
                        Layout.alignLeft(Fonts.bold(Labels.text(icon, text))));

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

        resetButton = Inputs.nofocus(Inputs.button(CarbonIcons.RESET, "Reset", "Reset entered values", () -> {
            MemoryParameter parameter = getSelectedParameter();
            if (parameter == null) {
                return;
            }

            if (JOptionPane.showConfirmDialog(getParent(),
                    "Are you sure you want to reset " + parameter.getName() + "?",
                    "Reset",
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }

            parameter.apply(list.getSelectedValue());
            dirtyMap.put(list.getSelectedValue(), false);
            saveButton.setEnabled(false);
            resetButton.setEnabled(false);
            Log.ui().log(Level.INFO, "Reset parameter definition back to project copy.");
            update();
        }));

        resetButton.setEnabled(isDirty());
        panel.add(resetButton);

        saveButton = Inputs.button(CarbonIcons.SAVE, "Save", "Save entered values", this::save);
        saveButton.setEnabled(isDirty());
        panel.add(Inputs.nofocus(saveButton));

        JButton copy = Inputs.nofocus(Inputs.button(CarbonIcons.COPY, "Copy", "Copy this parameter", () -> {
            MemoryParameter param = getSelectedParameter();
            if (param == null) {
                return;
            }

            String newParamName = param.getName() + " (Copy)";
            MemoryParameter newParam = param.copy();
            newParam.setName(newParamName);
            workingCopies.put(newParam, newParam);
            getParent().getActiveRom().addParameter(newParam);
            getParent().setDirty(true);

            update();
            updateListModel();
            list.setSelectedValue(newParam, true);

            getParent().getOpenWindows().forEach(Window::reload);
        }));
        panel.add(copy);

        boolean isNewTable = workingCopies.containsKey(getSelectedParameter());
        if (isNewTable) {
            dirtyMap.put(getSelectedParameter(), true);
            parameterChanged();
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
        MemoryParameter parameter = list.getSelectedValue();
        if (parameter == null) {
            return false;
        }

        Boolean result = dirtyMap.get(parameter);
        if (result == null) {
            return false;
        } else {
            return result;
        }
    }

    private void save() {
        MemoryParameter realParam = list.getSelectedValue();
        MemoryParameter workingParam = workingCopies.get(realParam);
        if (realParam == null || workingParam == null) {
            return;
        }

        if (realParam == workingParam) {
            // It's a new copy
            getParent().getActiveRom().addParameter(workingParam);
            workingCopies.put(realParam, realParam.copy());
        } else {
            realParam.apply(workingParam);
        }

        getParent().setDirty(true);
        dirtyMap.put(realParam, false);
        update();
        updateListModel();

        getParent().getOpenWindows().forEach(Window::reload);
    }

    private JPanel buildSettings() {
        MemoryParameter parameter = getSelectedParameter();
        JPanel settingsPanel = emptyBorder(0, 5, 0, 5, new JPanel(new BorderLayout()));
        addHeader(settingsPanel, CarbonIcons.SETTINGS, "Settings");

        JPanel content = new JPanel(new BorderLayout());
        JPanel inner = new JPanel(new GridBagLayout());
        Layout.topBorder(5, inner);

        JTextField nameField = Inputs.textField(parameter.getName(), (String)null, (name) -> {
                    parameter.setName(name);
                    parameterChanged();
                });
        createEntryRow(inner, 0, "Name", "Name of the parameter", nameField);

        MemoryAddressField memoryAddressField = Inputs.memoryAddressField(
                getParent().getActiveRom(),
                parameter.getAddress(),
                false, /* allow local and non-local */
                (newAddress) -> {
                    parameter.setAddress(newAddress);
                    parameterChanged();
                });


        ColorField colorField = new ColorField(getParent().getActiveRom(),
                parameter.getColor(), (newColor) -> {
            parameter.setColor(newColor);
            parameterChanged();
        });

        JComboBox<Scale> scaleField = Inputs.scaleField(
                getParent().getActiveRom(),
                parameter.getScale(),
                "The data scale and format for this parameter",
                (newScale) -> {
                    parameter.setScale(newScale);

                    // Shortcut to set the name of a series
                    if (newScale.getName() != null && !newScale.getName().isBlank() &&
                            (nameField.getText() == null || nameField.getText().isBlank())) {
                        nameField.setText(newScale.getName());
                    }

                    parameterChanged();
                }
        );

        Inputs.createEntryRow(inner, 1,
                "Address",
                "The data address for this parameter",
                memoryAddressField);

        Inputs.createEntryRow(inner, 2,
                "Format",
                "The format of the data in this parameter",
                scaleField);

        Inputs.createEntryRow(inner, 3,
                "Color",
                "The color of data for this parameter in data logging",
                colorField);

        createSaveRow(inner, 4);

        content.add(inner, BorderLayout.CENTER);
        matteBorder(1, 0, 0, 0, java.awt.Color.GRAY.darker(), content);

        settingsPanel.add(topBorder(5, wrap(new BorderLayout(), content, BorderLayout.NORTH)),
                BorderLayout.CENTER);

        return settingsPanel;
    }

    private String formatValue(float value, Unit unit) {
        if (value > 0 && value < 0.01f)
            value = 0.01f;
        return String.format("%.2f%s", value, unit.toString());
    }

    private JPanel buildInformation() {
        MemoryParameter param = getSelectedParameter();

        if (informationContent == null) {
            informationContent = emptyBorder(0, 5, 0, 5, new JPanel(new BorderLayout()));
        } else {
            informationContent.removeAll();
        }

        addHeader(informationContent, CarbonIcons.INFORMATION_SQUARE, "Info");

        JPanel content = new JPanel(new GridBagLayout());

        createEntryRow(
                content, 0, "Minimum", "Minimum value for this parameter",
                Labels.text(formatValue(param.getScale().getMinimum(), param.getScale().getUnit()), java.awt.Color.RED, Fonts.VALUE_FONT)
        );

        createEntryRow(
                content, 1, "Maximum", "Maximum value for this parameter",
                Labels.text(formatValue(param.getScale().getMaximum(), param.getScale().getUnit()), java.awt.Color.GREEN, Fonts.VALUE_FONT)
        );

        createEntryRow(
                content, 2, "Precision", "Precision of this parameter",
                Labels.text(formatValue(param.getScale().getPrecision(), param.getScale().getUnit()), Fonts.VALUE_FONT)
        );

        matteBorder(1, 0, 0, 0, java.awt.Color.GRAY.darker(), content);

        informationContent.add(topBorder(5, wrap(new BorderLayout(), content, BorderLayout.NORTH)),
            BorderLayout.CENTER);

        return informationContent;
    }

    private void buildView(JPanel parent) {
        parent.add(buildInformation(),
                gridBagConstraints(GridBagConstraints.CENTER,
                        GridBagConstraints.BOTH,
                        0, 0, 1, 1));

        parent.add(buildSettings(),
                gridBagConstraints(GridBagConstraints.CENTER,
                        GridBagConstraints.BOTH,
                        1, 0, 1, 1));
    }

    private void initView() {
        center.removeAll();
        getComponent().remove(center);

        if (!list.isSelectionEmpty()) {
            center.setLayout(new GridBagLayout());
            buildView(center);
            getComponent().add(emptyBorder(5, 5, 5, 5, center), BorderLayout.CENTER);
        }
    }

    public void parameterChanged() {
        MemoryParameter parameter = list.getSelectedValue();
        if (parameter == null) {
            return;
        }

        dirtyMap.put(parameter, true);
        saveButton.setEnabled(true);
        resetButton.setEnabled(true);

        updateInformation();

        getComponent().revalidate();
        getComponent().repaint();
    }

    public void updateListModel() {
        MemoryParameter selectedFormat = list.getSelectedValue();
        list.setModel(getParameterListModel());
        list.setSelectedValue(selectedFormat, true);

        list.revalidate();
        list.repaint();
    }

    public void update() {
        if (list.getSelectedValue() != null) {
            workingCopies.computeIfAbsent(list.getSelectedValue(), MemoryParameter::copy);
        }

        initView();

        getComponent().revalidate();
        getComponent().repaint();
    }

    @Override
    protected void initComponent(JPanel tab) {
        tab.setLayout(new BorderLayout());

        JPanel left = new JPanel(new BorderLayout());

        left.add(new ParametersTabToolbar(this).getComponent(), BorderLayout.NORTH);
        left.add(Layout.emptyBorder(scrollVertical(list = initParameterList())), BorderLayout.CENTER);

        tab.removeAll();
        tab.add(matteBorder(0, 0, 0, 1, java.awt.Color.GRAY.darker(), left), BorderLayout.WEST);

        center = new JPanel(new BorderLayout());

        // Autoselect the first item in the list
        if (list.isSelectionEmpty()) {
            list.setSelectedIndex(0);
        }
    }

    public MemoryParameter getSelectedParameter() {
        MemoryParameter realCopy = list.getSelectedValue();
        if (realCopy == null) {
            return null;
        }
        return workingCopies.get(realCopy);
    }

    @Override
    public String getTitle() {
        return "Parameters";
    }

    @Override
    public Icon getIcon() {
        return Icons.get(CarbonIcons.ACTIVITY, getTextColor());
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        update();
    }

    private void updateInformation() {
        buildInformation();
    }

    public void newParameter() {
        if (getParent().getActiveRom().getSections().stream().noneMatch(x -> x.getMemoryType() == MemoryType.RAM)) {
            JOptionPane.showMessageDialog(getParent(), "Please define any RAM memory " +
                            "section before adding a parameter.",
                    "No sections", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String newParameterName = (String) JOptionPane.showInputDialog(getParent().getParent(),
                "Specify a name", "New Parameter",
                QUESTION_MESSAGE, null, null, "New Parameter");
        if (newParameterName == null || newParameterName.isBlank()) {
            return;
        }

        MemoryParameter newParameter = new MemoryParameter();
        newParameter.setScale(Scale.NONE);
        newParameter.setName(newParameterName);
        newParameter.setAddress(getParent().getActiveRom().getDefaultMemoryAddress());
        workingCopies.put(newParameter, newParameter);

        update();
        updateListModel();
        list.setSelectedValue(newParameter, true);

        //TODO reload any datalogging
    }

    public void deleteParameter() {
        MemoryParameter parameter = list.getSelectedValue();
        if (parameter == null) {
            JOptionPane.showMessageDialog(getParent(), "No parameter was selected.",
                    "Delete", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (JOptionPane.showConfirmDialog(getParent(),
                "Are you sure you want to delete \"" + parameter.toString() + "\"?",
                "Delete operation",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        getParent().getActiveRom().removeParameter(parameter);

        update();
        updateListModel();

        getParent().getOpenWindows().forEach(Window::reload);
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
