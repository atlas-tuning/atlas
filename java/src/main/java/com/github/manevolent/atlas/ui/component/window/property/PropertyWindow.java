package com.github.manevolent.atlas.ui.component.window.property;

import com.github.manevolent.atlas.ui.Editor;
import com.github.manevolent.atlas.ui.component.window.Window;

import javax.swing.*;

public abstract class PropertyWindow extends JDialog  {
    private final Editor editor;

    protected PropertyWindow(Editor editor) {
        super(editor);

        this.editor = editor;
    }
}
