package com.github.manevolent.atlas.ui.settings;

import com.github.manevolent.atlas.model.KeyProperty;
import com.github.manevolent.atlas.model.Project;
import com.github.manevolent.atlas.model.PropertyDefinition;
import com.github.manevolent.atlas.model.uds.SecurityAccessProperty;
import com.github.manevolent.atlas.ui.settings.field.CheckboxSettingField;
import com.github.manevolent.atlas.ui.settings.field.KeySettingField;
import com.github.manevolent.atlas.ui.settings.field.SecurityAccessSettingField;
import com.github.manevolent.atlas.ui.settings.field.SettingField;
import com.github.manevolent.atlas.ui.util.Fonts;
import com.github.manevolent.atlas.ui.util.Labels;
import com.github.manevolent.atlas.ui.util.Layout;
import org.kordamp.ikonli.Ikon;

import javax.swing.*;
import java.awt.*;

public abstract class BasicSettingPage extends AbstractSettingPage {
    private final Frame parent;
    private JPanel content;

    private java.util.List<SettingField<?>> fields;

    public BasicSettingPage(Frame parent, Ikon icon, String name) {
        super(icon, name);

        this.parent = parent;
    }

    protected abstract java.util.List<SettingField<?>> createFields();

    private java.util.List<SettingField<?>> getFields() {
        if (this.fields == null) {
            this.fields = createFields();
        }

        return this.fields;
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

    private void initComponent() {
        JPanel content = Layout.emptyBorder(5, 5, 5, 5, new JPanel());
        content.setLayout(new GridBagLayout());

        addHeaderRow(content, 0, getIcon(), getName());

        java.util.List<SettingField<?>> elements = getFields();
        for (int row = 0; row < elements.size(); row ++) {
            SettingField<?> element = elements.get(row);

            String title = element instanceof CheckboxSettingField ? "" : element.getName();

            addEntryRow(content, 1 + row,
                    title, element.getTooltip(),
                    element.getInputComponent());
        }

        if (this.content == null) {
            this.content = new JPanel(new BorderLayout());
        }

        this.content.add(content, BorderLayout.NORTH);
    }

    @Override
    public JComponent getContent() {
        if (this.content == null) {
            initComponent();
        }

        return this.content;
    }

    public void reinitialize() {
        this.fields = createFields();
        JComponent content = getContent();
        content.removeAll();
        initComponent();

        content.revalidate();
        content.repaint();
    }

    @Override
    public boolean apply() {
        for (SettingField<?> element : getFields()) {
            if (!element.apply()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean isDirty() {
        return fields != null && fields.stream().anyMatch(SettingField::isDirty);
    }

    protected SettingField<?> createSettingField(PropertyDefinition definition, Project project) {
        if (definition.getValueType().equals(SecurityAccessProperty.class)) {
            return new SecurityAccessSettingField(parent, project,
                    definition.getKey(), definition.getName(),
                    definition.getDescription());
        } else if (definition.getValueType().equals(KeyProperty.class)) {
            return new KeySettingField(parent, project,
                    definition.getKey(), definition.getName(),
                    definition.getDescription());
        } else {
            throw new UnsupportedOperationException(definition.getValueType().getName());
        }
    }

}
