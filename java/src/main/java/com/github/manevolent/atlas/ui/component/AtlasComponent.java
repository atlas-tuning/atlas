package com.github.manevolent.atlas.ui.component;

import com.github.manevolent.atlas.ui.window.EditorForm;

import java.awt.*;

public abstract class AtlasComponent<T extends Component, E> {
    private final E parent;
    private final T component;

    private boolean initialized = false;

    ThreadLocal<Boolean> initializing = new ThreadLocal<>();

    protected AtlasComponent(E editor) {
        initializing.set(false);
        this.parent = editor;
        component = newComponent();
    }

    protected abstract T newComponent();

    public T getComponent() {
        if (!initialized && !initializing.get()) {
            try {
                initializing.set(true);
                preInitComponent(getComponent());
                initComponent(getComponent());
                postInitComponent(getComponent());
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

    public void reinitialize() {
        initComponent(getComponent());
        postInitComponent(getComponent());

        getComponent().revalidate();
        getComponent().repaint();
    }

}
