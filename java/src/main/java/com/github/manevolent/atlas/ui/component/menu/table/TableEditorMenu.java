package com.github.manevolent.atlas.ui.component.menu.table;

import com.github.manevolent.atlas.ui.component.TableEditorComponent;
import com.github.manevolent.atlas.ui.component.window.TableEditor;

import javax.swing.*;

public abstract class TableEditorMenu extends TableEditorComponent<JMenu> {
    protected TableEditorMenu(TableEditor editor) {
        super(editor);
    }

    @Override
    protected JMenu newComponent() {
        return new JMenu();
    }

    @Override
    protected void initComponent(JMenu component) {

    }
}
