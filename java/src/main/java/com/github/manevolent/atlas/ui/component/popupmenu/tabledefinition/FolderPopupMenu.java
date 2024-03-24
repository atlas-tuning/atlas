package com.github.manevolent.atlas.ui.component.popupmenu.tabledefinition;

import com.github.manevolent.atlas.model.Table;
import com.github.manevolent.atlas.ui.component.tab.TablesTab;
import com.github.manevolent.atlas.ui.util.Menus;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class FolderPopupMenu extends TablesTabPopupMenu {
    public FolderPopupMenu(TablesTab editor) {
        super(editor);
    }

    @Override
    protected void initComponent(JPopupMenu menu) {
        TablesTab tab = getParent();
        JTree tree = tab.getTree();

        menu.add(Menus.item(CarbonIcons.EXPAND_ALL, "Expand All", e -> {
            TreeNode selected = (TreeNode) tree.getLastSelectedPathComponent();
            tab.expandAll(selected);
        }));

        menu.addSeparator();

        menu.add(Menus.item(CarbonIcons.EDIT, "Rename Folder...", e -> {
            TreeNode selected = (TreeNode) tree.getLastSelectedPathComponent();
            tab.renameTables(selected);
        }));

        menu.add(Menus.item(CarbonIcons.DELETE, "Delete Folder", e -> {
            TreeNode selected = (TreeNode) tree.getLastSelectedPathComponent();
            tab.deleteTables(selected);
        }));
    }
}
