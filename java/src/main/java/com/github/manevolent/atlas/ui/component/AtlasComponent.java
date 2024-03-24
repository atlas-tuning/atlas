package com.github.manevolent.atlas.ui.component;

import com.github.manevolent.atlas.logging.Log;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;

public abstract class AtlasComponent<T extends Component, E> {
    private final E parent;
    private final T component;

    private boolean initialized = false;

    private ThreadLocal<Boolean> initializing = new ThreadLocal<>();

    protected AtlasComponent(E editor) {
        initializing.set(false);
        this.parent = editor;
        component = newComponent();
    }

    protected abstract T newComponent();

    public T getComponent() {
        if (!initialized && !initializing.get()) {
            Log.ui().log(Level.FINER, "Initializing component " + getClass().getName() + "...");
            try {
                initializing.set(true);
                preInitComponent(getComponent());
                initComponent(getComponent());
                postInitComponent(getComponent());
                Log.ui().log(Level.FINER, "Initialized component " + getClass().getName() + ".");
            } finally {
                initializing.set(false);
            }
            initialized = true;
        }

        return component;
    }

    public E getParent() {
        return parent;
    }

    protected void preInitComponent(T component) { }
    protected abstract void initComponent(T component);
    protected void postInitComponent(T component) { }

    protected Component getContent() {
        return getComponent();
    }

    public void reinitialize() {
        T component = getComponent();

        Component content = getContent();
        if (component instanceof JComponent) {
            ((JComponent) content).removeAll();
        }

        initComponent(component);
        postInitComponent(component);

        content.revalidate();
        content.repaint();
    }

}
