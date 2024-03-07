package com.github.manevolent.atlas.ui.window;

import com.github.manevolent.atlas.definition.MemoryAddress;
import com.github.manevolent.atlas.definition.MemorySection;
import com.github.manevolent.atlas.definition.Rom;
import com.github.manevolent.atlas.ui.Labels;
import com.github.manevolent.atlas.ui.Layout;

import javax.swing.*;
import java.awt.*;

import static com.github.manevolent.atlas.ui.Inputs.memorySectionField;

public class MemoryAddressDialog extends JDialog {
    private final Rom rom;
    private MemorySection section;
    private int offset;

    public MemoryAddressDialog(Rom rom, MemoryAddress address, Frame parent) {
        super(parent, "Select Region", true);

        this.rom = rom;
        this.section = address != null ? address.getSection() : null;
        this.offset = address != null ? address.getOffset() : 0;

        setType(Type.POPUP);
        initComponent();
        pack();
        setLocationRelativeTo(parent);
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

    private void initComponent() {
        JPanel content = createEntryPanel();

        createEntryRow(content, 1, "Region", "The ROM section this address will be relative to", memorySectionField(
            rom, section, (newSection) -> this.section = newSection
        ));

        getContentPane().add(content);
    }

    public MemoryAddress getAddress() {
        return MemoryAddress.builder()
                .withSection(section)
                .build();
    }

    public static MemoryAddress show(Frame parent, Rom rom, MemoryAddress current) {
        MemoryAddressDialog dialog = new MemoryAddressDialog(rom, current, parent);
        dialog.setVisible(true);
        return dialog.getAddress();
    }
}
