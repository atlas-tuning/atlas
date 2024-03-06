package com.github.manevolent.atlas.ui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class Layout {


    public static GridBagConstraints gridBagConstraints(int anchor, int fill,
                                                        int gridX, int gridY,
                                                        int sizeX, int sizey,
                                                        double weightX, double weightY) {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = anchor;
        gridBagConstraints.fill = fill;
        gridBagConstraints.gridx = gridX;
        gridBagConstraints.gridy = gridY;
        gridBagConstraints.gridwidth = sizeX;
        gridBagConstraints.gridheight = sizey;
        gridBagConstraints.weightx = weightX;
        gridBagConstraints.weighty = weightY;
        return gridBagConstraints;
    }

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

    public static GridBagConstraints gridBagTop(int width) {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 0;
        gridBagConstraints.gridwidth = width;
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

    public static <T extends JComponent> T border(Border border, T component) {
        // See: https://stackoverflow.com/questions/4335131/adding-border-to-jcheckbox
        if (component instanceof AbstractButton) {
            ((AbstractButton) component).setBorderPainted(true);
        }

        component.setBorder(border);
        return component;
    }

    public static <T extends JComponent> T matteBorder(int top, int left, int bottom, int right,
                                                       Color color,
                                                       T component) {
        return border(BorderFactory.createMatteBorder(top, left, bottom, right, color), component);
    }

    public static <T extends JComponent> T emptyBorder(int top, int left, int bottom, int right,
                                                       T component) {
        return border(BorderFactory.createEmptyBorder(top, left, bottom, right), component);
    }

    public static <T extends JComponent> T emptyBorder(T component) {
        return emptyBorder(0, 0, 0, 0, component);
    }

    public static <T extends JComponent> T topBorder(int top, T component) {
        return emptyBorder(top, 0, 0, 0, component);
    }

    public static <T extends JComponent> JScrollPane scroll(int vsbpolicy, int hsbpolicy, T component) {
        JScrollPane scrollPane = new JScrollPane(component, vsbpolicy, hsbpolicy);

        return scrollPane;
    }

    public static <T extends JComponent> JScrollPane scrollAsNeeded(T component) {
        return scroll(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED, component);
    }

    public static <T extends JComponent> JScrollPane scrollVertical(T component) {
        return scroll(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED, component);
    }

    public static <T extends JComponent> JScrollPane scrollHorizontal(T component) {
        return scroll(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS, component);
    }

    public static <T extends JComponent> JScrollPane scrollBoth(T component) {
        return scroll(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS, component);
    }

    public static <T extends JComponent> T minimumWidth(T component, int width) {
        Dimension preferredSize = component.getPreferredSize();
        component.setPreferredSize(new Dimension(
                Math.max(width, preferredSize.width),
                (int) preferredSize.getHeight()
        ));
        component.setMinimumSize(new Dimension(
                Math.max(width, preferredSize.width),
                (int) preferredSize.getHeight()
        ));
        return component;
    }

    public static <T extends JComponent> JPanel space(int top, int left, int bottom, int right, T component) {
        JPanel outer = new JPanel();
        JPanel space = new JPanel();
        emptyBorder(top, left, bottom, right, space);
        space.add(component);
        outer.add(space);
        return outer;
    }

    public static JPanel wrap(LayoutManager layout, JComponent component, Object layoutData) {
        JPanel wrapped = new JPanel(layout);
        wrapped.add(component, layoutData);
        return wrapped;
    }

}
