package com.github.manevolent.atlas.ui.component.tab;

import com.github.manevolent.atlas.model.Axis;
import com.github.manevolent.atlas.model.Table;
import com.github.manevolent.atlas.logging.Log;
import com.github.manevolent.atlas.ui.component.popupmenu.tabledefinition.FolderPopupMenu;
import com.github.manevolent.atlas.ui.component.popupmenu.tabledefinition.TablePopupMenu;
import com.github.manevolent.atlas.ui.component.window.TableDefinitionEditor;
import com.github.manevolent.atlas.ui.component.window.TableEditor;
import com.github.manevolent.atlas.ui.component.window.Window;
import com.github.manevolent.atlas.ui.util.Icons;
import com.github.manevolent.atlas.ui.Editor;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.github.manevolent.atlas.ui.util.Fonts.getTextColor;
import static javax.swing.JOptionPane.QUESTION_MESSAGE;

public class TablesTab
        extends Tab
        implements TreeSelectionListener, MouseListener {
    private JTree tree;
    private JTextField searchField;
    private Map<Table, TableNode> nodeMap = new HashMap<>();
    private Collection<TreePath> lastExpansions = new ArrayList<>();
    private DefaultTreeModel defaultModel;

    private JPopupMenu tablePopupMenu;
    private JPopupMenu folderPopupMenu;

    public TablesTab(Editor form, JTabbedPane tabbedPane) {
        super(form, tabbedPane);
    }

    @Override
    public String getTitle() {
        return "Tables";
    }

    @Override
    public Icon getIcon() {
        return Icons.get(CarbonIcons.TREE_VIEW_ALT, getTextColor());
    }

    public JTree getTree() {
        return tree;
    }

    public Table getTable(TreeNode node) {
        if (node instanceof TableNode) {
            return ((TableNode) node).table;
        }

        return null;
    }

    public TreePath getPath(TreeNode node) {
        if (node instanceof TableNode) {
            return getPath(((TableNode) node).table);
        }

        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        TreeNode[] nodes = model.getPathToRoot(node);
        return new TreePath(nodes);
    }

    public TreePath getPath(Table table) {
        TableNode node = nodeMap.get(table);
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        TreeNode[] nodes = model.getPathToRoot(node);
        return new TreePath(nodes);
    }

    private DefaultMutableTreeNode buildModel(String search) {
        DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode();
        List<Table> sortedTables = getParent().getProject().getTables().stream()
                .sorted(Comparator.comparing(Table::getName)).toList();
        for (Table table : sortedTables) {
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

    public void expandAll() {
        for (Table table : nodeMap.keySet()) {
            tree.makeVisible(getPath(table));
        }
    }

    public void expandAll(TreeNode node) {
        TreePath path = getPath(node);
        if (path != null) {
            tree.expandPath(path);
        }

        Enumeration<? extends TreeNode> children = node.children();
        if (children != null) {
            children.asIterator().forEachRemaining(this::expandAll);
        }

        tree.revalidate();
        getComponent().repaint();
    }

    public List<Table> getTablesUnder(TreeNode node) {
        if (node instanceof TableNode) {
            return Collections.singletonList(((TableNode) node).table);
        }

        List<Table> tables = new ArrayList<>();

        Enumeration<? extends TreeNode> children = node.children();
        if (children != null) {
            children.asIterator().forEachRemaining(child -> {
                tables.addAll(getTablesUnder(child));
            });
        }

        return tables;
    }

    public void renameTables(TreeNode node) {
        if (node == null || node instanceof TableNode) {
            return;
        }

        List<Table> tables = getTablesUnder(node);
        if (tables.isEmpty()) {
            return;
        }

        String oldFolderName = node.toString();
        String newFolderName = (String) JOptionPane.showInputDialog(getParent().getParent(),
                "Specify a name",
                "Rename Folder",
                QUESTION_MESSAGE, null, null, node.toString());

        if (newFolderName == null || newFolderName.isBlank()) {
            return;
        }

        List<String> nodes = new ArrayList<>();
        while ((node = node.getParent()) != null) {
            if (node.getParent() == null) break; // This is the root node, which has no name

            nodes.addFirst(node.toString());
        }

        List<String> oldNodes = new ArrayList<>(nodes);
        oldNodes.add(oldFolderName);
        String oldPrefix = String.join(" - ", oldNodes);

        List<String> newNodes = new ArrayList<>(nodes);
        newNodes.add(newFolderName);
        String newPrefix = String.join(" - ", newNodes);

        tables.forEach(table -> {
            String newName = table.getName().replaceFirst("^" + Pattern.quote(oldPrefix), newPrefix);
            table.setName(newName);
            getParent().setDirty(true);
        });

        update();

        getParent().getOpenWindows()
                .stream().filter(w -> w instanceof TableDefinitionEditor || w instanceof TableEditor)
                .forEach(Window::reload);
    }

    public void deleteTables(TreeNode node) {
        if (node == null || node instanceof TableNode) {
            return;
        }

        List<Table> tables = getTablesUnder(node);
        if (tables.isEmpty()) {
            return;
        }

        boolean allClosed = tables.stream()
                .allMatch(table -> getParent().getOpenWindows(table).stream().allMatch(Window::close));

        if (!allClosed) {
            return;
        }

        if (JOptionPane.showConfirmDialog(getParent(),
                "Are you sure you want to delete " + tables.size() + " table(s)?",
                "Delete Folder",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        tables.forEach(table -> {
            getParent().getProject().removeTable(table);
            Log.ui().log(Level.INFO, "Removed " + table.getName());
        });
        getParent().setDirty(true);

        update();
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
        tree.setCellRenderer(new Renderer());

        tablePopupMenu = new TablePopupMenu(this).getComponent();
        folderPopupMenu = new FolderPopupMenu(this).getComponent();

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

    public void open(TreePath selPath) {
        if (selPath == null) {
            return;
        }

        Object last = selPath.getLastPathComponent();

        if (last instanceof TableNode) {
            getParent().openTable(((TableNode)last).table);
        }
    }

    public void define(TreePath selPath) {
        if (selPath == null) {
            return;
        }

        Object last = selPath.getLastPathComponent();

        if (last instanceof TableNode) {
            getParent().openTableDefinition(((TableNode)last).table);
        }
    }

    public void defineCopy(TreePath selPath) {
        if (selPath == null) {
            return;
        }

        Object last = selPath.getLastPathComponent();

        if (last instanceof TableNode) {
            Table table = ((TableNode)last).table;
            table = table.copy();

            String newTableName = (String) JOptionPane.showInputDialog(getParent().getParent(),
                    "Specify a name",
                    "New Table",
                    QUESTION_MESSAGE, null, null, table.getName() + " (Copy)");
            if (newTableName == null || newTableName.isBlank()) {
                return;
            }
            table.setName(newTableName);
            getParent().openTableDefinition(table);
        }
    }

    public void focusSearch() {
        searchField.grabFocus();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() != 2) {
            return;
        }

        TreePath selPath = tree.getSelectionPath();
        if (selPath != null && e.getButton() == MouseEvent.BUTTON1) {
            open(selPath);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if(SwingUtilities.isRightMouseButton(e)){
            int selRow = tree.getClosestRowForLocation(e.getX(), e.getY());
            TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
            tree.setSelectionPath(selPath);
            if (selRow > -1){
                tree.setSelectionRow(selRow);
            }

            selPath = tree.getSelectionPath();
            Object last = selPath != null ? selPath.getLastPathComponent() : null;
            if (last instanceof TableNode) {
                tree.setComponentPopupMenu(tablePopupMenu);
            } else if (last != null) {
                tree.setComponentPopupMenu(folderPopupMenu);
            } else {
                tree.setComponentPopupMenu(null);
            }
        }

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

    private static class TableNode implements MutableTreeNode {
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

    private class Renderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                                                      boolean leaf, int row, boolean hasFocus) {
            JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            if (value instanceof TableNode node) {
                if (node.table.hasAxis(Axis.X) || node.table.hasAxis(Axis.Y)) {
                    label.setIcon(Icons.get(CarbonIcons.DATA_TABLE));
                } else {
                    label.setIcon(Icons.get(CarbonIcons.STRING_INTEGER));
                }
            }

            return label;
        }
    }

}
