package com.github.manevolent.atlas.ui;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class Menus {

    public static JMenuItem item(Ikon ikon, String text, ActionListener actionListener) {
        JMenuItem menuItem = new JMenuItem(text);
        menuItem.setIcon(Icons.get(ikon, menuItem.getForeground()));
        menuItem.addActionListener(actionListener);
        return menuItem;
    }

}
