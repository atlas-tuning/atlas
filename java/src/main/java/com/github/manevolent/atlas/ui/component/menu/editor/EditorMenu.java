package com.github.manevolent.atlas.ui.component.menu.editor;

import com.github.manevolent.atlas.ui.component.EditorComponent;
import com.github.manevolent.atlas.ui.Editor;

import javax.swing.*;

public abstract class EditorMenu extends EditorComponent<JMenu> {
    protected EditorMenu(Editor editor) {
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
