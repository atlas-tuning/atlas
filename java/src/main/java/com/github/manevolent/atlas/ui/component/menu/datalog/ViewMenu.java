package com.github.manevolent.atlas.ui.component.menu.datalog;

import com.github.manevolent.atlas.ui.component.window.DatalogWindow;

import javax.swing.*;

public class ViewMenu extends DatalogMenu {
    public ViewMenu(DatalogWindow editor) {
        super(editor);
    }

    @Override
    protected void initComponent(JMenu fileMenu) {
        fileMenu.setText("View");

        JMenuItem viewAll = new JMenuItem("View all");
        viewAll.addActionListener(e -> {

        });
    }
}
