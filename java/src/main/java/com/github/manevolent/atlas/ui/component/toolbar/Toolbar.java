package com.github.manevolent.atlas.ui.component.toolbar;

import com.github.manevolent.atlas.ui.component.AtlasComponent;

import javax.swing.*;
import java.awt.*;

public abstract class Toolbar<E> extends AtlasComponent<JToolBar, E> {
    protected Toolbar(E editor) {
        super(editor);
    }

    @Override
    protected JToolBar newComponent() {
        JToolBar toolBar = new JToolBar();
        toolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY.darker()));
        return toolBar;
    }
}
