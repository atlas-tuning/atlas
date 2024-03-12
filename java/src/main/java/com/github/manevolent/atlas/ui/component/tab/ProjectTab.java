package com.github.manevolent.atlas.ui.component.tab;

import com.github.manevolent.atlas.connection.ConnectionType;
import com.github.manevolent.atlas.model.*;
import com.github.manevolent.atlas.ui.*;
import com.github.manevolent.atlas.ui.util.*;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import java.awt.*;
import java.awt.Color;

public class ProjectTab extends Tab {
    private JPanel center;

    private boolean dirty;

    private JButton resetButton;
    private JButton saveButton;

    private JComboBox<ConnectionType> connectionTypeField;

    public ProjectTab(Editor editor, JTabbedPane tabbedPane) {
        super(editor, tabbedPane);
    }

    @Override
    public String getTitle() {
        return "Project";
    }

    @Override
    public Icon getIcon() {
        return Icons.get(CarbonIcons.PRODUCT);
    }

    public Project getProject() {
        return getParent().getProject();
    }

    public Vehicle getVehicle() {
        return getParent().getProject().getVehicle();
    }

    private void modelUpdated() {
        getParent().updateTitle();
        getParent().setDirty(true);
    }

    @Override
    protected void initComponent(JPanel component) {
        component.setLayout(new BorderLayout());

        JPanel content = Layout.emptyBorder(5, 5, 5, 5, new JPanel());
        content.setLayout(new GridBagLayout());
        addHeaderRow(content, 0, CarbonIcons.CAR, "Vehicle Information");

        addEntryRow(content, 1, "Year", "The year of the vehicle",
                Inputs.textField(getVehicle().getYear(), s -> {
                    getVehicle().setYear(s);
                    modelUpdated();
                }));

        addEntryRow(content, 2, "Market", "The market of the vehicle",
                Inputs.textField(getVehicle().getMarket(), s -> {
                    getVehicle().setMarket(s);
                    modelUpdated();
                }));

        addEntryRow(content, 3, "Make", "The make of the vehicle",
                Inputs.textField(getVehicle().getMake(), s -> {
                    getVehicle().setMake(s);
                    modelUpdated();
                }));

        addEntryRow(content, 4, "Model", "The model of the vehicle",
                Inputs.textField(getVehicle().getModel(), s -> {
                    getVehicle().setModel(s);
                    modelUpdated();
                }));

        addEntryRow(content, 5, "Trim", "The trim of the vehicle",
                Inputs.textField(getVehicle().getTrim(), s -> {
                    getVehicle().setTrim(s);
                    modelUpdated();
                }));

        addEntryRow(content, 6, "Model", "The transmission of the vehicle",
                Inputs.textField(getVehicle().getTransmission(), s -> {
                    getVehicle().setTransmission(s);
                    modelUpdated();
                }));

        addHeaderRow(content, 7, CarbonIcons.PLUG_FILLED, "Connection");

        addEntryRow(content, 8, "Type", "The communication type for connections to this vehicle",
                connectionTypeField = Inputs.connectionTypeField(null, getProject().getConnectionType(), t -> {
                    ConnectionType existing = getProject().getConnectionType();
                    if (t != existing) {
                        if (JOptionPane.showConfirmDialog(getParent(),
                                "Are you sure you want to change the project connection type?\r\n" +
                                "Doing so will interrupt any established connections.",
                                "Warning",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                            connectionTypeField.setSelectedItem(existing);
                            return;
                        }
                        Job.fork(() -> {
                            getProject().setConnectionType(t);
                            getParent().reestablishConnection();
                        });
                        modelUpdated();
                    }
                }));

        content.add(Box.createVerticalGlue(), Layout.gridBagConstraints(
                GridBagConstraints.NORTHWEST, GridBagConstraints.VERTICAL, 0, 9, 1, 1
        ));

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        component.add(scrollPane, BorderLayout.CENTER);
    }

    private JComponent addHeaderRow(JPanel entryPanel, int row,
                                    Ikon icon, String label) {
        JPanel labelPanel = Layout.wrap(Layout.emptyBorder(5, 0, 5, 0, Fonts.bold(Labels.text(icon, label))));
        Layout.matteBorder(0, 0, 1, 0, Color.GRAY.darker(), labelPanel);
        labelPanel = Layout.emptyBorder(0, 0, 5, 0, Layout.wrap(labelPanel));

        entryPanel.add(labelPanel, Layout.gridBagConstraints(
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                0, row,
                2, 1,
                1, 0
        ));
        return labelPanel;
    }

    private JComponent addEntryRow(JPanel entryPanel, int row,
                                      String label, String helpText,
                                      JComponent input) {
        // Label
        JLabel labelField = Labels.darkerText(label);
        entryPanel.add(labelField, Layout.gridBagConstraints(
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                0, row,
                0, 0
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
                        GridBagConstraints.HORIZONTAL,
                        1, row,
                        1, 0));

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
}
