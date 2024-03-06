package com.github.manevolent.atlas.ui.component.tab;

import com.github.manevolent.atlas.definition.DataFormat;
import com.github.manevolent.atlas.definition.Scale;
import com.github.manevolent.atlas.definition.Unit;
import com.github.manevolent.atlas.ui.Icons;
import com.github.manevolent.atlas.ui.Inputs;
import com.github.manevolent.atlas.ui.Labels;
import com.github.manevolent.atlas.ui.Layout;
import com.github.manevolent.atlas.ui.component.toolbar.FormatsTabToolbar;
import com.github.manevolent.atlas.ui.window.EditorForm;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.Comparator;

import static com.github.manevolent.atlas.ui.Fonts.getTextColor;
import static com.github.manevolent.atlas.ui.Layout.*;

public class FormatsTab extends Tab implements ListSelectionListener {
    private JList<Scale> list;
    private JPanel center;

    public FormatsTab(EditorForm editor) {
        super(editor);
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
        list.setCellRenderer(new Renderer());
        list = Layout.minimumWidth(list, 200);
        list.addListSelectionListener(this);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return Layout.emptyBorder(list);
    }

    private void addHeader(JPanel panel, Ikon icon, String text) {
        panel.add(Layout.matteBorder(0, 0, 1, 0, Color.GRAY.darker(),
                        Layout.space(0, 2, 0, 2, Labels.text(icon, text))),
                BorderLayout.NORTH
        );
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

    private JPanel buildSettings() {
        Scale scale = getSelectedScale();
        JPanel settingsPanel = emptyBorder(0, 5, 0, 5, new JPanel(new BorderLayout()));
        addHeader(settingsPanel, CarbonIcons.SETTINGS, "Settings");

        JPanel content = new JPanel(new GridBagLayout());

        JTextField nameField = Inputs.textField(scale.getName(), scale::setName);
        createEntryRow(content, 0, "Name", "Name of the format", nameField);

        JComboBox<Unit> unitField = Inputs.unitField(scale.getName(),
                scale.getUnit(), scale::setUnit);
        createEntryRow(content, 1, "Unit", null, unitField);

        JComboBox<DataFormat> dataType = Inputs.dataTypeField(scale.getName(),
                scale.getFormat(), scale::setFormat);
        createEntryRow(content, 2, "Data Type", null, dataType);

        settingsPanel.add(topBorder(5, wrap(new BorderLayout(), content, BorderLayout.NORTH)),
                BorderLayout.CENTER);
        return settingsPanel;
    }

    private JPanel buildOperations() {
        JPanel operationsPanel = emptyBorder(0, 5, 0, 5, new JPanel(new BorderLayout()));
        addHeader(operationsPanel, CarbonIcons.CALCULATOR, "Operations");
        return operationsPanel;
    }

    private String formatValue(float value, Unit unit) {
        if (value > 0 && value < 0.01f)
            value = 0.01f;
        return String.format("%.2f%s", value, unit.toString());
    }

    private JPanel buildInformation() {
        Scale scale = getSelectedScale();

        JPanel usagesPanel = emptyBorder(0, 5, 0, 5, new JPanel(new BorderLayout()));
        addHeader(usagesPanel, CarbonIcons.INFORMATION_SQUARE, "Info");

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

        usagesPanel.add(topBorder(5, wrap(new BorderLayout(), content, BorderLayout.NORTH)),
                BorderLayout.CENTER);
        return usagesPanel;
    }

    private JPanel buildUsages() {
        JPanel usagesPanel = emptyBorder(0, 5, 0, 5, new JPanel(new BorderLayout()));
        addHeader(usagesPanel, CarbonIcons.TABLE, "Usages");



        return usagesPanel;
    }

    private void buildView(JPanel parent) {
        parent.add(buildSettings());
        parent.add(buildOperations());
        parent.add(buildInformation());
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

    public void update() {
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
        return list.getSelectedValue();
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

    private class Renderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            return component;
        }
    }
}
