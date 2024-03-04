package com.github.manevolent.atlas.ui;

import javax.swing.*;
import java.awt.*;

public class Separators {

    public static Color getColor() {
        return Color.GRAY.darker();
    }

    public static JSeparator vertical() {
        Color color = getColor();
        JSeparator separator = new JSeparator(JSeparator.VERTICAL);
        separator.setBackground(color);
        return separator;
    }

}
