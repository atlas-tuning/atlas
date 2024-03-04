package com.github.manevolent.atlas.ui.component.menu.table;

import com.github.manevolent.atlas.ui.component.window.TableEditor;

import javax.swing.*;

public class ViewMenu extends TableEditorMenu {
    public ViewMenu(TableEditor editor) {
        super(editor);
    }

    @Override
    protected void initComponent(JMenu menu) {
        menu.setText("View");
    }
}
