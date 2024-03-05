package com.github.manevolent.atlas.ui.component.toolbar;

import com.github.manevolent.atlas.ui.Icons;
import com.github.manevolent.atlas.ui.component.AtlasComponent;
import org.kordamp.ikonli.Ikon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

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

    protected JButton makeButton(Ikon ikon, int size, String actionCommand, String toolTipText,
                                 ActionListener listener) {
        JButton add = new JButton(Icons.get(ikon, Color.WHITE.darker(), size));
        add.setActionCommand(actionCommand);
        add.setToolTipText(toolTipText);
        if (listener != null) {
            add.addActionListener(listener);
        }
        return add;
    }

    protected JButton makeButton(Ikon ikon, String actionCommand, String toolTipText,
                                 ActionListener listener) {
        return makeButton(ikon, 18, actionCommand, toolTipText, listener);
    }

    protected JButton makeButton(Ikon ikon, String actionCommand, String toolTipText) {
        return makeButton(ikon, actionCommand, toolTipText, null);
    }

    protected JButton makeSmallButton(Ikon ikon, String actionCommand, String toolTipText,
                                      ActionListener listener) {
        return makeButton(ikon, 14, actionCommand, toolTipText, listener);
    }

    protected JButton makeSmallButton(Ikon ikon, String actionCommand, String toolTipText) {
        return makeSmallButton(ikon, actionCommand, toolTipText, null);
    }
}
