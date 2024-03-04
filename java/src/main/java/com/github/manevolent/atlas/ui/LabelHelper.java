package com.github.manevolent.atlas.ui;

import org.kordamp.ikonli.Ikon;

import javax.swing.*;
import java.awt.*;

public class LabelHelper {

    public static JLabel text(String text) {
        JLabel label = new JLabel(text);
        return label;
    }

    public static JLabel text(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        return label;
    }

    public static JLabel text(Ikon icon, String text, Color color) {
        JLabel label = new JLabel(text);
        label.setIcon(IconHelper.get(icon, color));
        label.setForeground(color);
        return label;
    }


    public static JLabel text(Ikon icon, String text) {
        JLabel label = new JLabel(text);
        label.setIcon(IconHelper.get(icon, label.getForeground()));
        return label;
    }

    public static JLabel text(Ikon icon, Color iconColor, String text, Color color) {
        JLabel label = new JLabel(text);
        label.setIcon(IconHelper.get(icon, iconColor));
        label.setForeground(color);
        return label;
    }

    public static JLabel text(Ikon icon, Color iconColor, Font font, String text, Color color) {
        JLabel label = new JLabel(text);
        label.setIcon(IconHelper.get(icon, iconColor));
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

}
