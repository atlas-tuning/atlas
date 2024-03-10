package com.github.manevolent.atlas.ui.component.tab;

import com.github.manevolent.atlas.ui.component.EditorComponent;
import com.github.manevolent.atlas.ui.Editor;

import javax.swing.*;

public abstract class Tab extends EditorComponent<JPanel> {
    private JTabbedPane pane;

    protected Tab(Editor editor, JTabbedPane pane) {
        super(editor);

        this.pane = pane;
    }

    protected JTabbedPane getPane() {
        return pane;
    }

    public void focus() {
        pane.setSelectedComponent(getComponent());
    }

    @Override
    public JPanel newComponent() {
        JPanel panel = new JPanel();
        return panel;
    }

    public abstract String getTitle();
    public abstract Icon getIcon();
}
