package com.github.manevolent.atlas.ui.component.popupmenu.tabledefinition;

import com.github.manevolent.atlas.model.Table;
import com.github.manevolent.atlas.ui.component.tab.TablesTab;
import com.github.manevolent.atlas.ui.util.Menus;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import javax.swing.tree.TreeNode;

public class TablePopupMenu extends TablesTabPopupMenu {
    public TablePopupMenu(TablesTab editor) {
        super(editor);
    }

    @Override
    protected void initComponent(JPopupMenu menu) {
        TablesTab tab = getParent();
        JTree tree = tab.getTree();

        menu.add(Menus.item(CarbonIcons.LAUNCH, "Open Table", e -> {
            TreeNode lastSelected = (TreeNode) tree.getLastSelectedPathComponent();
            tab.open(tab.getPath(lastSelected));
        }));

        menu.addSeparator();

        menu.add(Menus.item(CarbonIcons.DATA_TABLE_REFERENCE, "New Table...", e -> {
            tab.getParent().newTable();
        }));

        menu.addSeparator();

        menu.add(Menus.item(CarbonIcons.CHART_CUSTOM, "Edit Definition...", e -> {
            TreeNode lastSelected = (TreeNode) tree.getLastSelectedPathComponent();
            tab.define(tab.getPath(lastSelected));
        }));
        menu.add(Menus.item(CarbonIcons.COPY, "Copy Definition...", e -> {
            TreeNode lastSelected = (TreeNode) tree.getLastSelectedPathComponent();
            tab.defineCopy(tab.getPath(lastSelected));
        }));

        menu.addSeparator();

        menu.add(Menus.item(CarbonIcons.DELETE, "Delete Table", e -> {
            Table toDelete = tab.getTable((TreeNode) tab.getTree().getLastSelectedPathComponent());
            if (toDelete == null) {
                return;
            }

            if (JOptionPane.showConfirmDialog(tab.getParent(),
                    "Are you sure you want to delete " + toDelete.getName() + "?",
                    "Delete Table",
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }

            tab.getParent().getProject().removeTable(toDelete);
            tab.update();
        }));
    }
}
