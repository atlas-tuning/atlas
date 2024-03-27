package com.github.manevolent.atlas.ui.settings;

import com.github.manevolent.atlas.ui.settings.validation.ValidationProblem;
import com.github.manevolent.atlas.ui.util.Colors;
import com.github.manevolent.atlas.ui.util.Icons;
import com.github.manevolent.atlas.ui.util.Layout;

import com.google.common.html.HtmlEscapers;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

public class ProblemsSettingPage extends AbstractSettingPage {
    private final java.util.List<ValidationProblem> problems = new ArrayList<>();

    private final SettingsDialog<?> dialog;
    private final JPanel panel;
    private JScrollPane scrollPane;

    protected ProblemsSettingPage(SettingsDialog<?> dialog) {
        super(CarbonIcons.WARNING_FILLED, "Problems");

        this.dialog = dialog;
        this.panel = new JPanel();
        this.panel.setLayout(new GridBagLayout());
    }

    public void setProblems(Collection<ValidationProblem> problems) {
        this.problems.clear();
        this.problems.addAll(
                problems.stream()
                .sorted(Comparator.comparing(v -> v.getSeverity().getOrdinal()))
                .toList()
        );

        reload();
    }

    private JPanel createRow(Ikon ikon, Color color, String message) {
        JLabel label = new JLabel();
        label.setIcon(Icons.get(ikon, color));

        JPanel outerPanel = new JPanel(new BorderLayout());
        Layout.emptyBorder(5, 5, 0, 5, outerPanel);

        JPanel innerPanel = new JPanel(new BorderLayout());
        Layout.emptyBorder(5, 5, 5, 5, innerPanel);
        innerPanel.setBackground(Colors.withAlpha(color, 16));

        String escaped = HtmlEscapers.htmlEscaper().escape(message);
        escaped = escaped.replace("\r\n", "<br/>");
        label.setText("<html>" + escaped + "</html>");
        innerPanel.add(label);

        outerPanel.add(innerPanel);

        return outerPanel;
    }

    private void reload() {
        panel.removeAll();

        int row = 0;
        for (; row < problems.size(); row ++) {
            ValidationProblem problem = problems.get(row);

            Ikon ikon = problem.getSeverity().getIkon();
            JPanel rowPanel = createRow(ikon, problem.getSeverity().getColor(), problem.getErrorMessage());

            if (problem.getPage() != null) {
                rowPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                rowPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        dialog.selectPage(problem.getPage());
                    }
                });
            }

            panel.add(rowPanel,
                    Layout.gridBagConstraints(GridBagConstraints.NORTH,
                    GridBagConstraints.HORIZONTAL, 0, row, 1, 0));
        }

        if (problems.isEmpty()) {
            JPanel rowPanel = createRow(CarbonIcons.CHECKMARK, Color.GREEN, "No problems were found");
            panel.add(rowPanel,
                    Layout.gridBagConstraints(GridBagConstraints.NORTH,
                            GridBagConstraints.HORIZONTAL, 0, row, 1, 0));
        }

        panel.add(Box.createVerticalGlue(),
                Layout.gridBagConstraints(GridBagConstraints.NORTH,
                GridBagConstraints.HORIZONTAL, 0, problems.size(), 1, 1));

        SwingUtilities.invokeLater(() -> {
            panel.revalidate();
            panel.repaint();
        });
    }

    @Override
    public JComponent getContent() {
        reload();

        scrollPane = new JScrollPane(
                panel,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        scrollPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                JScrollPane scrollPane = (JScrollPane) e.getComponent();
                panel.setMaximumSize(new Dimension(0, Integer.MAX_VALUE));
                Dimension preferredSize = panel.getLayout().minimumLayoutSize(panel);
                panel.setPreferredSize(new Dimension(
                        0,
                        (int) preferredSize.getHeight()
                ));
                panel.revalidate();
            }
        });

        Layout.emptyBorder(scrollPane);

        return scrollPane;
    }

    @Override
    public boolean apply() {
        // Do nothing, we're just a problems page
        return true;
    }

    @Override
    public boolean isDirty() {
        // Never dirty
        return false;
    }

    @Override
    public boolean isScrollNeeded() {
        // We must define our own scroll pane
        return false;
    }
}
