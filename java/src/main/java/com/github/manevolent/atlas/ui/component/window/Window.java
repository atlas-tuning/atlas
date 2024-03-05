package com.github.manevolent.atlas.ui.component.window;

import com.github.manevolent.atlas.ui.component.AtlasComponent;
import com.github.manevolent.atlas.ui.component.EditorComponent;
import com.github.manevolent.atlas.ui.window.EditorForm;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyVetoException;

public abstract class Window extends EditorComponent<JInternalFrame> {
    protected Window(EditorForm editor) {
        super(editor);
    }

    @Override
    protected void postInitComponent(JInternalFrame component) {
        component.pack();
    }

    public abstract String getTitle();
    public abstract Icon getIcon();

    @Override
    protected JInternalFrame newComponent() {
        JInternalFrame internalFrame = new JInternalFrame();

        internalFrame.setMinimumSize(new Dimension(300, 200));
        internalFrame.setPreferredSize(new Dimension(300, 200));

        internalFrame.setClosable(true);
        internalFrame.setMaximizable(true);
        internalFrame.setIconifiable(false);
        internalFrame.setResizable(true);

        Icon icon = getIcon();

        try {
            internalFrame.setIcon(icon != null);

            if (icon != null) {
                internalFrame.setFrameIcon(icon);
            }
        } catch (PropertyVetoException e) {
            throw new RuntimeException(e);
        }

        return internalFrame;
    }

    @Override
    protected void preInitComponent(JInternalFrame component) {
        super.preInitComponent(component);
        component.setTitle(getTitle());
    }

    public void focus() {
        JDesktopPane desktop = getParent().getDesktop();
        JInternalFrame component = getComponent();

        if (!component.getParent().equals(desktop)) {
            return;
        }

        component.setVisible(true);
        component.grabFocus();
        desktop.moveToFront(component);

        try {
            component.setSelected(true);
        } catch (PropertyVetoException e) {
            throw new RuntimeException(e);
        }
    }
}
