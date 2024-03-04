package com.github.manevolent.atlas.ui.component.menu.table;

import com.github.manevolent.atlas.ui.component.window.TableEditor;

import javax.swing.*;

public class EditMenu extends TableEditorMenu {
    public EditMenu(TableEditor editor) {
        super(editor);
    }

    @Override
    protected void initComponent(JMenu menu) {
        menu.setText("Edit");
    }
}
