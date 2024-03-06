package com.github.manevolent.atlas.ui.component.tab;

import com.github.manevolent.atlas.definition.Table;
import com.github.manevolent.atlas.logging.Log;
import com.github.manevolent.atlas.ui.Icons;
import com.github.manevolent.atlas.ui.Menus;
import com.github.manevolent.atlas.ui.window.EditorForm;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.List;
import java.util.logging.Level;

public class TablesTab
        extends Tab
        implements TreeSelectionListener, MouseListener {
    private JTree tree;
    private JTextField searchField;
    private Map<Table, TableNode> nodeMap = new HashMap<>();
    private Collection<TreePath> lastExpansions = new ArrayList<>();
    private DefaultTreeModel defaultModel;

    public TablesTab(EditorForm form) {
        super(form);
    }

    @Override
    public String getTitle() {
        return "Tables";
    }

    @Override
    public Icon getIcon() {
        return Icons.get(CarbonIcons.DATA_TABLE, Color.WHITE);
    }

    private Table getTable(TreeNode node) {
        if (node instanceof TableNode) {
            return ((TableNode) node).table;
        }

        return null;
    }

    private TreePath getPath(TreeNode node) {
        if (node instanceof TableNode) {
            return getPath(((TableNode) node).table);
        }

        return null;
    }

    private TreePath getPath(Table table) {
        TableNode node = nodeMap.get(table);
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        TreeNode[] nodes = model.getPathToRoot(node);
        return new TreePath(nodes);
    }

    private DefaultMutableTreeNode buildModel(String search) {
        DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode();
        for (Table table : getParent().getActiveRom().getTables()) {
            if (search != null && !table.getName().toLowerCase().contains(search.toLowerCase())) {
                continue;
            }

            List<String> items = Arrays.stream(table.getName().split("\\-"))
                    .map(String::trim).toList();
            MutableTreeNode parent = treeRoot;
            for (int i = 0 ; i < items.size(); i ++) {
                String text = items.get(i);

                // Find an existing child, if possible
                MutableTreeNode child = Collections.list(Objects.requireNonNull(parent.children()))
                        .stream()
                        .filter(x -> x instanceof DefaultMutableTreeNode)
                        .map(x -> (DefaultMutableTreeNode)x)
                        .filter(x -> x.getUserObject().toString().equals(text))
                        .findFirst()
                        .orElse(null);

                if (child == null) {
                    boolean isLeaf = i == items.size() - 1;
                    if (isLeaf) {
                        TableNode tableNode = new TableNode(table, text);
                        nodeMap.put(table, tableNode);
                        child = tableNode;
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

        if (treeRoot.getChildCount() <= 0 && search != null) {
            DefaultMutableTreeNode noResults = new DefaultMutableTreeNode("No results");
            treeRoot.add(noResults);
        }

        return treeRoot;
    }

    private void search(String searchText) {
        tree.setModel(new DefaultTreeModel(buildModel(searchText)));

        if (!searchText.isEmpty()) {
            expandAll();
        } else {
            reexpand();
        }

        tree.revalidate();
        getComponent().repaint();
    }

    private void expandAll() {
        for (Table table : nodeMap.keySet()) {
            tree.makeVisible(getPath(table));
        }
    }

    private void reexpand() {
        Log.ui().log(Level.FINER, "Re-expanding tables (" + lastExpansions.size() + " expansions)");
        tree.setModel(defaultModel);

        for (TreePath path : lastExpansions) {
            Log.ui().log(Level.FINER, "Re-expanding " + Arrays.toString(path.getPath()));
            tree.expandPath(path);
            tree.makeVisible(path);
        }
    }

    private void updateExpansions() {
        if (!searchField.getText().isEmpty()) {
            return;
        }

        lastExpansions = Collections.list(tree.getExpandedDescendants(
                new TreePath(tree.getModel().getRoot())));
    }

    protected void initComponent(JPanel panel) {
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // Search
        searchField = new JTextField();
        searchField.setToolTipText("Search tables");
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                updateExpansions();
            }

            @Override
            public void keyTyped(KeyEvent e) {
                search(searchField.getText());
            }
        });
        panel.add(searchField, BorderLayout.SOUTH);

        // Tree
        tree = new JTree();
        tree.addTreeSelectionListener(this);
        tree.addMouseListener(this);

        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(Menus.item(CarbonIcons.LAUNCH, "Open Table", e -> {
            TreeNode lastSelected = (TreeNode) tree.getLastSelectedPathComponent();
            open(getPath(lastSelected));
        }));
        popupMenu.addSeparator();
        popupMenu.add(Menus.item(CarbonIcons.CHART_CUSTOM, "Edit Definition", e -> {
            TreeNode lastSelected = (TreeNode) tree.getLastSelectedPathComponent();
            define(getPath(lastSelected));
        }));
        popupMenu.add(Menus.item(CarbonIcons.COPY, "Copy Definition", e -> {
            TreeNode lastSelected = (TreeNode) tree.getLastSelectedPathComponent();
            defineCopy(getPath(lastSelected));
        }));
        popupMenu.addSeparator();
        popupMenu.add(Menus.item(CarbonIcons.DELETE, "Delete Table", e -> {
            Table toDelete = getTable((TreeNode) tree.getLastSelectedPathComponent());
            if (toDelete == null) {
                return;
            }

            if (JOptionPane.showConfirmDialog(getParent(),
                    "Are you sure you want to delete " + toDelete.getName() + "?",
                    "Delete Table",
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }

            getParent().getActiveRom().removeTable(toDelete);

            update();
        }));
        tree.setComponentPopupMenu(popupMenu);

        // You can only be focused on one table at a time
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        nodeMap.clear();

        tree.setModel(defaultModel = new DefaultTreeModel(buildModel(null)));
        tree.setBackground(new Color(0, 0, 0, 0));
        tree.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

        JPanel treePanel = new JPanel();

        treePanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        treePanel.add(tree, c);

        JScrollPane scrollPane = new JScrollPane(
                treePanel,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        scrollPane.setViewportBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        panel.add(scrollPane, BorderLayout.CENTER);
    }

    public void tableOpened(Table table) {
        TableNode node = nodeMap.get(table);
        if (node == null) {
            return;
        }

        TreePath path = getPath(table);
        tree.getSelectionModel().setSelectionPath(path);
        tree.makeVisible(path);
        tree.scrollPathToVisible(path);

        updateExpansions();

        // Bugfix for UI elements that seemingly repaint in a strange way
        this.getComponent().repaint();
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        updateExpansions();
    }

    public void update() {
        tree.setModel(defaultModel = new DefaultTreeModel(buildModel(null)));

        getComponent().revalidate();
        getComponent().repaint();
    }


    private void open(TreePath selPath) {
        if (selPath == null) {
            return;
        }

        Object last = selPath.getLastPathComponent();

        if (last instanceof TableNode) {
            getParent().openTable(((TableNode)last).table);
        }
    }

    private void define(TreePath selPath) {
        if (selPath == null) {
            return;
        }

        Object last = selPath.getLastPathComponent();

        if (last instanceof TableNode) {
            getParent().openTableDefinition(((TableNode)last).table);
        }
    }

    private void defineCopy(TreePath selPath) {
        if (selPath == null) {
            return;
        }

        Object last = selPath.getLastPathComponent();

        if (last instanceof TableNode) {
            Table table = ((TableNode)last).table;
            table = table.copy();
            table.setName(table.getName() + " (Copy)");
            getParent().openTableDefinition(table);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
        tree.setSelectionPath(selPath);

        if (e.getClickCount() != 2) {
            return;
        }

        if (e.getButton() == MouseEvent.BUTTON1) {
            open(selPath);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // Bugfix for UI elements that seemingly repaint in a strange way
        this.getComponent().repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Bugfix for UI elements that seemingly repaint in a strange way
        this.getComponent().repaint();
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
        private TreeNode parent;

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
            return parent;
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
            this.parent = newParent;
        }

        @Override
        public int hashCode() {
            return table.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TableNode) {
                return equals((TableNode)obj);
            } else {
                return super.equals(obj);
            }
        }

        public boolean equals(TableNode obj) {
            return obj.table.equals(table);
        }
    }

}
