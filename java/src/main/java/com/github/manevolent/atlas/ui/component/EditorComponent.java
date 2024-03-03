package com.github.manevolent.atlas.ui.component;

import com.github.manevolent.atlas.ui.window.EditorForm;

import javax.swing.*;
import java.awt.*;

public abstract class EditorComponent<T extends Component> {
    private final EditorForm editor;
    private final T component;

    private boolean initialized = false;

    ThreadLocal<Boolean> initializing = new ThreadLocal<>();

    protected EditorComponent(EditorForm editor) {
        initializing.set(false);
        this.editor = editor;
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

    public EditorForm getEditor() {
        return editor;
    }

    protected void preInitComponent(T component) { }
    protected abstract void initComponent(T component);
    protected void postInitComponent(T component) { }

}
