package com.github.manevolent.atlas.ui.component.tab;

import com.github.manevolent.atlas.ui.component.AtlasComponent;
import com.github.manevolent.atlas.ui.component.EditorComponent;
import com.github.manevolent.atlas.ui.window.EditorForm;

import javax.swing.*;

public abstract class Tab extends EditorComponent<JPanel> {
    protected Tab(EditorForm editor) {
        super(editor);
    }

    @Override
    public JPanel newComponent() {
        JPanel panel = new JPanel();
        return panel;
    }

    public abstract String getTitle();
    public abstract Icon getIcon();
}
