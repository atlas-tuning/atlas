package com.github.manevolent.atlas.ui.component.toolbar;

import com.github.manevolent.atlas.ui.util.Fonts;
import com.github.manevolent.atlas.ui.util.Icons;
import com.github.manevolent.atlas.ui.component.AtlasComponent;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public abstract class Toolbar<E> extends AtlasComponent<JToolBar, E> {
    protected Toolbar(E editor) {
        super(editor);
    }

    public static int LARGE_BUTTON_ICON_SIZE = 22;
    public static int BUTTON_ICON_SIZE = 18;
    public static int BUTTON_SMALL_ICON_SIZE = 14;

    @Override
    protected JToolBar newComponent() {
        JToolBar toolBar = new JToolBar();
        toolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY.darker()));
        return toolBar;
    }

    protected JButton makeButton(Ikon ikon, int size, String actionCommand, String toolTipText,
                                 ActionListener listener) {
        Color enabledColor = Fonts.getTextColor();
        Color disabledColor = Fonts.getTextColor().darker();

        JButton button = new JButton(Icons.get(ikon, enabledColor, size)) {
            @Override
            public void setEnabled(boolean b) {
                super.setEnabled(b);

                if (b) {
                    ((FontIcon)getIcon()).setIconColor(enabledColor);
                } else {
                    ((FontIcon)getIcon()).setIconColor(disabledColor);
                }
            }
        };

        //noinspection SuspiciousNameCombination
        button.setMinimumSize(new Dimension(
                button.getPreferredSize().height,
                button.getPreferredSize().height
        ));

        //noinspection SuspiciousNameCombination
        button.setPreferredSize(new Dimension(
                button.getPreferredSize().height,
                button.getPreferredSize().height
        ));

        //noinspection SuspiciousNameCombination
        button.setMaximumSize(new Dimension(
                button.getPreferredSize().height,
                button.getPreferredSize().height
        ));

        button.setActionCommand(actionCommand);
        button.setToolTipText(toolTipText);
        if (listener != null) {
            button.addActionListener(listener);
        }

        return button;
    }

    protected JButton makeLargeButton(Ikon ikon, String actionCommand, String toolTipText,
                                 ActionListener listener) {
        return makeButton(ikon, LARGE_BUTTON_ICON_SIZE, actionCommand, toolTipText, listener);
    }

    protected JButton makeButton(Ikon ikon, String actionCommand, String toolTipText,
                                 ActionListener listener) {
        return makeButton(ikon, BUTTON_ICON_SIZE, actionCommand, toolTipText, listener);
    }

    protected JButton makeButton(Ikon ikon, String actionCommand, String toolTipText) {
        return makeButton(ikon, actionCommand, toolTipText, null);
    }

    protected JButton makeSmallButton(Ikon ikon, String actionCommand, String toolTipText,
                                      ActionListener listener) {
        return makeButton(ikon, BUTTON_SMALL_ICON_SIZE, actionCommand, toolTipText, listener);
    }

    protected JButton makeSmallButton(Ikon ikon, String actionCommand, String toolTipText) {
        return makeSmallButton(ikon, actionCommand, toolTipText, null);
    }
}
