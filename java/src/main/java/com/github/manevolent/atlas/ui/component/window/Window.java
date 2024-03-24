package com.github.manevolent.atlas.ui.component.window;

import com.github.manevolent.atlas.ui.component.EditorComponent;
import com.github.manevolent.atlas.ui.Editor;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.*;
import java.beans.PropertyVetoException;

public abstract class Window extends EditorComponent<JInternalFrame> {
    private boolean iconified = false;

    protected Window(Editor editor) {
        super(editor);
    }

    @Override
    protected void preInitComponent(JInternalFrame component) {
        super.preInitComponent(component);
        component.setTitle(getTitle());
        component.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameDeiconified(InternalFrameEvent e) {
                iconified = false;
            }

            @Override
            public void internalFrameIconified(InternalFrameEvent e) {
                iconified = true;
            }
        });
    }

    @Override
    protected void postInitComponent(JInternalFrame component) {
        if (!component.isVisible()) {
            component.pack();

            component.setPreferredSize(component.getPreferredSize());
            component.setSize(component.getPreferredSize());

            //TODO this most likely will annoy people, make a setting for it
            try {
                component.setMaximum(true);
            } catch (PropertyVetoException e) {
                // Ignore
            }
        }

        if (component.isVisible()) {
            getComponent().revalidate();
            getComponent().repaint();
        }
    }

    public abstract String getTitle();

    public abstract Icon getIcon();

    public void updateTitle() {
        getComponent().setFrameIcon(getIcon());
        getComponent().setTitle(getTitle());
    }

    public abstract void reload();

    @Override
    protected JInternalFrame newComponent() {
        JInternalFrame internalFrame = new JInternalFrame() {
            @Override
            public void setTitle(String title) {
                super.setTitle(title);
                Window.this.getParent().getWindowMenu().update();
            }
        };

        internalFrame.setMinimumSize(new Dimension(300, 200));
        internalFrame.setPreferredSize(new Dimension(300, 200));

        internalFrame.setClosable(true);
        internalFrame.setMaximizable(true);
        internalFrame.setIconifiable(true);
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

    public boolean isMinimized() {
        return iconified;
    }

    public void focus() {
        JDesktopPane desktop = getParent().getDesktop();
        JInternalFrame component = getComponent();

        if (component.getParent() != null && !component.getParent().equals(desktop)) {
            return;
        }

        try {
            component.setIcon(false);
        } catch (PropertyVetoException e) {
            throw new RuntimeException(e);
        }

        component.setVisible(true);
        desktop.moveToFront(component);
        component.grabFocus();

        try {
            component.setSelected(true);
        } catch (PropertyVetoException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Component getContent() {
        return getComponent().getContentPane();
    }

    @Override
    public String toString() {
        return getTitle();
    }

    public boolean close() {
        JInternalFrame component = getComponent();
        component.doDefaultCloseAction();
        return component.isClosed();
    }
}
