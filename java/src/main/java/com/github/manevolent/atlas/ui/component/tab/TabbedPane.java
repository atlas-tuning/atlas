package com.github.manevolent.atlas.ui.component.tab;

import com.github.manevolent.atlas.ui.component.AtlasComponent;
import com.github.manevolent.atlas.ui.component.EditorComponent;
import com.github.manevolent.atlas.ui.window.EditorForm;

import javax.swing.*;

public class TabbedPane extends EditorComponent<JTabbedPane> {
    public TabbedPane(EditorForm editor, Tab... tabs) {
        super(editor);
    }

    @Override
    protected JTabbedPane newComponent() {
        return new JTabbedPane();
    }

    @Override
    protected void initComponent(JTabbedPane component) {

    }

    public void addTab(Tab tab) {
        getComponent().addTab(tab.getTitle(), tab.getIcon(), tab.getComponent());
    }
}
