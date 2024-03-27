package com.github.manevolent.atlas.ui.settings;

import com.github.manevolent.atlas.ApplicationMetadata;
import com.github.manevolent.atlas.ui.settings.validation.ValidationProblem;
import com.github.manevolent.atlas.ui.settings.validation.ValidationSeverity;
import com.github.manevolent.atlas.ui.settings.validation.ValidationState;
import com.github.manevolent.atlas.ui.util.Icons;
import com.github.manevolent.atlas.ui.util.Inputs;
import com.github.manevolent.atlas.ui.util.Layout;
import org.kordamp.ikonli.Ikon;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

public abstract class SettingsDialog<T> extends JDialog implements TreeSelectionListener,
        PropertyChangeListener, KeyEventDispatcher {
    private final T settingObject;
    private final String title;

    private final ProblemsSettingPage problems;

    private final java.util.List<SettingPage> pages;
    private final Map<SettingPage, TreeNode> nodes = new HashMap<>();

    private JTree tree;
    private JPanel treePanel;
    private JPanel settingContentPanel;
    private JLabel problemLabel;
    private JButton apply;
    private DefaultTreeModel treeModel;

    private SettingPage currentPage;

    public SettingsDialog(Ikon ikon, String title, Frame parent, T object) {
        super(parent, ApplicationMetadata.getName() + " - " + title, true);

        this.title = title;
        this.settingObject = object;

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancel();
            }
        });

        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addPropertyChangeListener("permanentFocusOwner", this);

        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                        .addKeyEventDispatcher(this);

        setPreferredSize(new Dimension(800, 600));

        setIconImage(Icons.getImage(ikon, Color.WHITE).getImage());

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        pages = new ArrayList<>(getPages());

        pages.add(problems = new ProblemsSettingPage(this));

        initComponent();

        pack();

        setLocationRelativeTo(parent);
        setResizable(true);
        setModalityType(ModalityType.DOCUMENT_MODAL);
    }

    public T getSettingObject() {
        return settingObject;
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        Object last = e.getPath().getLastPathComponent();
        if (last instanceof SettingPageNode) {
            SettingPage page = ((SettingPageNode) last).getSettingPage();
            openPage(page);
        }

        treePanel.repaint();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        apply.setEnabled(isDirty());
        validatePages();
    }

    public void selectPage(SettingPage settingPage) {
        TreePath path = new TreePath(treeModel.getPathToRoot(nodes.get(settingPage)));
        tree.setSelectionPath(path);
    }

    private void openPage(SettingPage settingPage) {
        settingContentPanel.removeAll();

        this.currentPage = settingPage;
        validatePages();

        JComponent content = settingPage.getContent();
        if (settingPage.isScrollNeeded()) {
            JScrollPane scrollPane = new JScrollPane(content);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            settingContentPanel.add(scrollPane, BorderLayout.CENTER);
        } else {
            settingContentPanel.add(content, BorderLayout.CENTER);
        }

        settingContentPanel.revalidate();
        settingContentPanel.repaint();
    }

    protected abstract java.util.List<SettingPage> getPages();

    private JComponent initContent() {
        settingContentPanel = new JPanel(new BorderLayout());

        return settingContentPanel;
    }

    private DefaultTreeModel createModel() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(title);

        pages.forEach(page -> {
            MutableTreeNode node = new SettingPageNode(page);
            nodes.put(page, node);
            root.add(node);
        });

        return treeModel = new DefaultTreeModel(root);
    }

    private JComponent initTree() {
        tree = new JTree(createModel());

        tree.addTreeSelectionListener(this);
        tree.setCellRenderer(new Renderer());
        tree.setBackground(new Color(0, 0, 0, 0));

        tree.setSelectionModel(new DefaultTreeSelectionModel() {
            @Override
            public void setSelectionPaths(TreePath[] pPaths) {
                TreePath[] filtered = Arrays.stream(pPaths)
                        .filter(path -> path.getLastPathComponent() instanceof SettingPageNode)
                        .toArray(TreePath[]::new);

                if (filtered.length == 0) {
                    return;
                }

                super.setSelectionPaths(filtered);
            }
        });

        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        Layout.preferWidth(tree, 200);

        JPanel treePanel = new JPanel();

        treePanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        treePanel.add(tree, c);

        JScrollPane scrollPane = new JScrollPane(
                treePanel,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        scrollPane.getVerticalScrollBar().addAdjustmentListener(e -> {
            scrollPane.revalidate();
            scrollPane.repaint();
        });

        scrollPane.getHorizontalScrollBar().addAdjustmentListener(e -> {
            scrollPane.revalidate();
            scrollPane.repaint();
        });

        scrollPane.setViewportBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        this.treePanel = new JPanel(new BorderLayout());
        Layout.matteBorder(0, 0, 0, 1, Color.GRAY.darker(), this.treePanel);
        this.treePanel.add(scrollPane, BorderLayout.CENTER);
        return this.treePanel;
    }

    private JComponent initFooter() {
        JPanel footer = new JPanel(new GridLayout(1, 2));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        Layout.emptyBorder(5, 5, 5, 5, footer);

        JButton ok;
        buttonRow.add(ok = Inputs.nofocus(Inputs.button("OK", this::ok)));
        buttonRow.add(Inputs.nofocus(Inputs.button("Cancel", this::cancel)));
        buttonRow.add(apply = Inputs.nofocus(Inputs.button("Apply", this::apply)));

        getRootPane().setDefaultButton(ok);

        problemLabel = new JLabel();
        problemLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        problemLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tree.setSelectionRow(tree.getRowCount() - 1);
            }
        });

        footer.add(problemLabel);
        footer.add(buttonRow);

        return footer;
    }

    public boolean isDirty() {
        return pages.stream().anyMatch(SettingPage::isDirty);
    }

    private void cancel() {
        if (isDirty()) {
            String message = "You have unsaved changes to your settings " +
                    "that will be lost. Save changes before closing?";

            int answer = JOptionPane.showConfirmDialog(getParent(),
                    message,
                    "Unsaved changes",
                    JOptionPane.YES_NO_CANCEL_OPTION
            );

            switch (answer) {
                case JOptionPane.CANCEL_OPTION:
                    return;
                case JOptionPane.YES_OPTION:
                    ApplyResult result = apply();
                    if (result != ApplyResult.SUCCESS && result != ApplyResult.NOTHING_APPLIED) {
                        return;
                    }
                case JOptionPane.NO_OPTION:
                    break;
            }
        }

        this.dispose();
    }

    private void ok() {
        ApplyResult result = apply();
        if (result == ApplyResult.SUCCESS || result == ApplyResult.NOTHING_APPLIED) {
            dispose();
        }
    }

    /**
     *
     * @return
     */
    protected ApplyResult apply() {
        ValidationState state = validatePages();
        if (state != null && state.willBlockApply()) {
            JOptionPane.showMessageDialog(getParent(),
                    "Failed to save settings!\r\n" +
                            "There are outstanding errors that must be" +
                            " addressed before settings can be saved.\r\n" +
                    "See the Problems page for more details.",
                    "Settings Save Failed",
                    JOptionPane.ERROR_MESSAGE);

            return ApplyResult.FAILED_VALIDATION;
        }

        boolean applied = false;
        for (SettingPage page : pages) {
            if (!page.isDirty()) {
                continue;
            }

            if (!page.apply()) {
                return ApplyResult.FAILED_APPLY;
            }

            applied = true;
        }

        apply.setEnabled(isDirty());

        return applied ? ApplyResult.SUCCESS : ApplyResult.NOTHING_APPLIED;
    }

    private void initComponent() {
        JPanel contentPanel = new JPanel(new BorderLayout());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, initTree(), initContent());
        Layout.matteBorder(1, 0, 1, 0, Color.GRAY.darker(), splitPane);
        contentPanel.add(splitPane, BorderLayout.CENTER);
        contentPanel.add(initFooter(), BorderLayout.SOUTH);

        setContentPane(contentPanel);

        if (tree.getRowCount() > 1) {
            tree.setSelectionRow(1);
        }
    }

    private void updateProblemLabel(ValidationProblem problem) {
        if (problem == null) {
            problemLabel.setIcon(null);
            problemLabel.setText(null);
            return;
        }

        ValidationSeverity severity = problem.getSeverity();

        problemLabel.setIcon(Icons.get(severity.getIkon(), severity.getColor()));
        problemLabel.setText(problem.getErrorMessage().split("\r\n")[0]);

        SwingUtilities.invokeLater(() -> {
            problemLabel.revalidate();
            problemLabel.repaint();
        });
    }

    private ValidationState validatePages() {
        if (this.pages == null) {
            return null;
        }

        if (currentPage != null) {
            ValidationState state = currentPage.validate();

            ValidationProblem problem = state.getProblems().stream()
                    .min(Comparator.comparing(v -> v.getSeverity().getOrdinal()))
                    .orElse(null);

            updateProblemLabel(problem);
        }

        ValidationState state = new ValidationState();
        pages.forEach(page -> page.validate(state));
        problems.setProblems(state.getProblems());
        return state;
    }

    @Override
    public void dispose() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .removePropertyChangeListener("permanentFocusOwner", this);

        super.dispose();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getID()  == KeyEvent.KEY_PRESSED) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                cancel();
                return true;
            }
        }

        return false;
    }

    private static class SettingPageNode implements MutableTreeNode {
        private final SettingPage settingPage;
        private TreeNode parent;

        private SettingPageNode(SettingPage settingPage) {
            this.settingPage = settingPage;
        }

        public SettingPage getSettingPage() {
            return settingPage;
        }

        @Override
        public TreeNode getChildAt(int childIndex) {
            return null;
        }

        @Override
        public int getChildCount() {
            return 0;
        }

        @Override
        public TreeNode getParent() {
            return parent;
        }

        @Override
        public void setParent(MutableTreeNode newParent) {
            this.parent = newParent;
        }

        @Override
        public int getIndex(TreeNode node) {
            return 0;
        }

        @Override
        public boolean getAllowsChildren() {
            return false;
        }

        @Override
        public boolean isLeaf() {
            return true;
        }

        @Override
        public Enumeration<? extends TreeNode> children() {
            return null;
        }

        @Override
        public String toString() {
            return settingPage.getName();
        }

        @Override
        public void insert(MutableTreeNode child, int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void remove(int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void remove(MutableTreeNode node) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setUserObject(Object object) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeFromParent() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int hashCode() {
            return settingPage.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof SettingPageNode) {
                return equals((SettingPageNode)obj);
            } else {
                return super.equals(obj);
            }
        }

        public boolean equals(SettingPageNode obj) {
            return obj.settingPage.equals(settingPage);
        }
    }

    private class Renderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                                                      boolean leaf, int row, boolean hasFocus) {
            JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            Layout.emptyBorder(2, 5, 2, 5, label);
            label.setFont(label.getFont().deriveFont(13f));

            if (value instanceof SettingPageNode) {
                SettingPage page = ((SettingPageNode) value).getSettingPage();
                label.setIcon(Icons.get(page.getIcon(), 13));
                label.setText(page.getName());
                label.setFont(label.getFont().deriveFont(Font.PLAIN));

                return label;
            } else {

                label.setFont(label.getFont().deriveFont(Font.BOLD));

                return label;
            }
        }
    }

    public enum ApplyResult {
        FAILED_VALIDATION,
        FAILED_APPLY,
        SUCCESS,
        NOTHING_APPLIED
    }
}
