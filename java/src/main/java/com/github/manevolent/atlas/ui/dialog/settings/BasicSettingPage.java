package com.github.manevolent.atlas.ui.dialog.settings;

import com.github.manevolent.atlas.ui.util.Fonts;
import com.github.manevolent.atlas.ui.util.Inputs;
import com.github.manevolent.atlas.ui.util.Labels;
import com.github.manevolent.atlas.ui.util.Layout;
import org.kordamp.ikonli.Ikon;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BasicSettingPage extends AbstractSettingPage {
    private final java.util.List<Element> elements;
    private JPanel content;

    public BasicSettingPage(Ikon icon, String name, List<Element> elements) {
        super(icon, name);

        this.elements = elements;
    }

    public BasicSettingPage(Ikon icon, String name, Element... elements) {
        this(icon, name, Arrays.asList(elements));
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

        for (int row = 0; row < elements.size(); row ++) {
            Element element = elements.get(row);
            addEntryRow(content, 1 + row, element.getName(), element.getTooltip(), element.getInputComponent());
        }

        this.content = new JPanel(new BorderLayout());
        this.content.add(content, BorderLayout.NORTH);
    }

    @Override
    public JComponent getContent() {
        if (this.content == null) {
            initComponent();
        }

        return this.content;
    }

    @Override
    public void apply() {
        for (Element<?> element : elements) {
            element.apply();
        }
    }

    public interface Element<V> {
        String getName();
        String getTooltip();
        JComponent getInputComponent();
        V apply();
    }

    private abstract static class AbstractElement<V> implements Element<V> {
        private final String name, tooltip;

        private AbstractElement(String name, String tooltip) {
            this.name = name;
            this.tooltip = tooltip;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getTooltip() {
            return tooltip;
        }
    }

    public static class TextElement extends AbstractElement<String> {
        private final Consumer<String> apply;
        private final JTextField textField;

        public TextElement(String name,
                           String tooltip,
                           String defaultValue,
                           Consumer<String> apply) {
            super(name, tooltip);

            this.apply = apply;
            this.textField = Inputs.textField(defaultValue, (text) -> { /*ignore*/ });
        }

        @Override
        public JComponent getInputComponent() {
            return textField;
        }

        @Override
        public String apply() {
            String value = textField.getText();
            apply.accept(value);
            return value;
        }
    }
}
