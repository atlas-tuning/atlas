package com.github.manevolent.atlas.ui.component.footer;

import com.github.manevolent.atlas.ui.component.AtlasComponent;

import javax.swing.*;

/**
 * Simple parent class for footers
 * @param <T> editor/parent type
 */
public abstract class Footer<T> extends AtlasComponent<JPanel, T> {
    protected Footer(T editor) {
        super(editor);
    }

    @Override
    protected JPanel newComponent() {
        return new JPanel();
    }
}
