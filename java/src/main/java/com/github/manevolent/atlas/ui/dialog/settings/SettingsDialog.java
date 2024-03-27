package com.github.manevolent.atlas.ui.dialog.settings;

import com.github.manevolent.atlas.ApplicationMetadata;
import com.github.manevolent.atlas.ui.util.Icons;
import com.github.manevolent.atlas.ui.util.Inputs;
import com.github.manevolent.atlas.ui.util.Layout;
import org.kordamp.ikonli.Ikon;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

import java.util.Enumeration;

public abstract class SettingsDialog<T> extends JDialog implements TreeSelectionListener {
    private final T settingObject;
    private final String title;

    private JTree tree;
    private JPanel treePanel;
    private JPanel settingContentPanel;
    private DefaultTreeModel treeModel;

    private java.util.List<SettingPage> pages;

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

        setPreferredSize(new Dimension(800, 600));

        setIconImage(Icons.getImage(ikon, Color.WHITE).getImage());

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        pages = getPages();

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

    private void openPage(SettingPage settingPage) {
        settingContentPanel.removeAll();

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

        pages.forEach(page -> root.add(new SettingPageNode(page)));

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
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        Layout.emptyBorder(5, 5, 5, 5, footer);

        JButton ok;
        footer.add(ok = Inputs.nofocus(Inputs.button("OK", this::ok)));
        footer.add(Inputs.nofocus(Inputs.button("Cancel", this::cancel)));
        footer.add(Inputs.nofocus(Inputs.button("Apply", this::apply)));

        getRootPane().setDefaultButton(ok);

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
                    if (apply() != ApplyResult.SUCCESS) {
                        return;
                    }
                case JOptionPane.NO_OPTION:
                    break;
            }
        }

        this.dispose();
    }

    private void ok() {
        if (apply() == ApplyResult.SUCCESS) {
            this.dispose();
        }
    }

    /**
     *
     * @return
     */
    protected ApplyResult apply() {
        if (!pages.stream().allMatch(SettingPage::validate)) {
            return ApplyResult.FAILED_VALIDATION;
        }

        for (SettingPage page : pages) {
            if (!page.apply()) {
                return ApplyResult.FAILED_APPLY;
            }
        }

        return ApplyResult.SUCCESS;
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
        SUCCESS
    }
}
