package com.github.manevolent.atlas.ui.component.menu;

import com.github.manevolent.atlas.ui.component.EditorComponent;
import com.github.manevolent.atlas.ui.window.EditorForm;

import javax.swing.*;

public abstract class Menu extends EditorComponent<JMenu> {
    protected Menu(EditorForm editor) {
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
