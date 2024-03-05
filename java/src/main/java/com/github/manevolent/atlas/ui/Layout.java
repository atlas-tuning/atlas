package com.github.manevolent.atlas.ui;

import javax.swing.*;
import java.awt.*;

public class Layout {

    public static GridBagConstraints gridBagConstraints(int anchor, int fill,
                                             int gridX, int gridY,
                                             double weightX, double weightY) {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = anchor;
        gridBagConstraints.fill = fill;
        gridBagConstraints.gridx = gridX;
        gridBagConstraints.gridy = gridY;
        gridBagConstraints.weightx = weightX;
        gridBagConstraints.weighty = weightY;
        return gridBagConstraints;
    }

    public static GridBagConstraints gridBagTop() {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 0;
        return gridBagConstraints;
    }

    public static <T extends JComponent> T alignTop(T component) {
        component.setAlignmentY(Component.TOP_ALIGNMENT);
        return component;
    }

    public static <T extends JComponent> T alignBottom(T component) {
        component.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        return component;
    }

    public static <T extends JComponent> T alignLeft(T component) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        return component;
    }

    public static <T extends JComponent> T alignRight(T component) {
        component.setAlignmentX(Component.RIGHT_ALIGNMENT);
        return component;
    }

    public static <T extends JPanel> Box leftJustify(T panel)  {
        Box b = Box.createHorizontalBox();
        b.add(panel);
        b.add(Box.createHorizontalGlue());
        return b;
    }

    public static Box leftJustify(Component... panels)  {
        Box b = Box.createHorizontalBox();
        for (Component component : panels) {
            b.add(component);
        }
        b.add(Box.createHorizontalGlue());
        return b;
    }

}
