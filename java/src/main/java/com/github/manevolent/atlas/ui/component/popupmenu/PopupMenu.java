package com.github.manevolent.atlas.ui.component.popupmenu;

import com.github.manevolent.atlas.ui.component.AtlasComponent;

import javax.swing.*;

public abstract class PopupMenu<E> extends AtlasComponent<JPopupMenu, E> {
    protected PopupMenu(E editor) {
        super(editor);
    }

    @Override
    protected JPopupMenu newComponent() {
        return new JPopupMenu();
    }
}
