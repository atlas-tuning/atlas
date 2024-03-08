package com.github.manevolent.atlas.ui.component.menu.editor;

import com.github.manevolent.atlas.ui.component.EditorComponent;
import com.github.manevolent.atlas.ui.EditorForm;

import javax.swing.*;

public abstract class EditorMenu extends EditorComponent<JMenu> {
    protected EditorMenu(EditorForm editor) {
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
