package com.github.manevolent.atlas.ui.component.window;

import com.github.manevolent.atlas.ui.component.EditorComponent;
import com.github.manevolent.atlas.ui.window.EditorForm;

import javax.swing.*;
import java.awt.*;

public abstract class Window extends EditorComponent<JInternalFrame> {
    protected Window(EditorForm editor) {
        super(editor);
    }

    @Override
    protected void postInitComponent(JInternalFrame component) {
        component.pack();
    }

    @Override
    protected JInternalFrame newComponent() {
        JInternalFrame internalFrame = new JInternalFrame();

        internalFrame.setMinimumSize(new Dimension(300, 200));
        internalFrame.setPreferredSize(new Dimension(300, 200));

        internalFrame.setClosable(true);
        internalFrame.setMaximizable(true);
        internalFrame.setIconifiable(false);
        internalFrame.setResizable(true);

        return internalFrame;
    }
}
