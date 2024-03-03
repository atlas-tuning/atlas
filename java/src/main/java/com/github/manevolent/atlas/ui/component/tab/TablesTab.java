package com.github.manevolent.atlas.ui.component.tab;

import com.github.manevolent.atlas.definition.Table;
import com.github.manevolent.atlas.ui.window.EditorForm;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class TablesTab
        extends Tab
        implements TreeSelectionListener, MouseListener {
    private JTree tree;

    public TablesTab(EditorForm form) {
        super(form);
    }

    protected void initComponent(JPanel panel) {
        tree = new JTree();

        tree.addTreeSelectionListener(this);
        tree.addMouseListener(this);

        // You can only be focused on one table at a time
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode();
        for (Table table : getEditor().getActiveRom().getTables()) {
            List<String> items = Arrays.stream(table.getName().split("\\-"))
                    .map(String::trim).toList();
            MutableTreeNode parent = treeRoot;
            for (int i = 0 ; i < items.size(); i ++) {
                String text = items.get(i);

                // Find an existing child, if possible
                MutableTreeNode child = Collections.list(parent.children())
                        .stream()
                        .filter(x -> x instanceof DefaultMutableTreeNode)
                        .map(x -> (DefaultMutableTreeNode)x)
                        .filter(x -> x.getUserObject().toString().equals(text))
                        .findFirst()
                        .orElse(null);

                if (child == null) {
                    boolean isLeaf = i == items.size() - 1;
                    if (isLeaf) {
                        child = new TableNode(table, text);
                    } else {
                        child = new DefaultMutableTreeNode(text);
                    }

                    if (parent instanceof DefaultMutableTreeNode) {
                        ((DefaultMutableTreeNode) parent).add(child);
                    } else {
                        throw new UnsupportedOperationException();
                    }
                }

                parent = child;
            }
        }

        tree.setModel(new DefaultTreeModel(treeRoot));
        tree.setBackground(new Color(0, 0, 0, 0));
        tree.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        JPanel treePanel = new JPanel();
        treePanel.add(tree);

        JScrollPane scrollPane = new JScrollPane(
                treePanel,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        scrollPane.setViewportBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        panel.setLayout(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    }

    public void tableOpened(Table table) {
        //TODO switch TablesTab active table
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int selRow = tree.getRowForLocation(e.getX(), e.getY());
        TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());

        if (e.getClickCount() != 2) {
            return;
        }

        if (selPath == null) {
            return;
        }

        Object last = selPath.getLastPathComponent();

        if (last instanceof TableNode) {
            getEditor().openTable(((TableNode)last).table);
        }
    }



    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    private class TableNode implements MutableTreeNode {
        private final Table table;
        private final String text;

        private TableNode(Table table, String text) {
            this.table = table;
            this.text = text;
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
            return null;
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
            return text;
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
        public void setParent(MutableTreeNode newParent) {

        }
    }

}
