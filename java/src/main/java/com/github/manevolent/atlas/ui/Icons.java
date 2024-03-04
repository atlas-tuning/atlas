package com.github.manevolent.atlas.ui;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import java.awt.*;

public class Icons {
    private static final int ICON_SIZE = 14;

    public static FontIcon get(Ikon ikon, Color color) {
        FontIcon icon = new FontIcon();
        icon.setIkon(ikon);
        icon.setIconColor(color);
        icon.setIconSize(ICON_SIZE);
        return icon;
    }


    public static FontIcon get(Ikon ikon) {
        FontIcon icon = new FontIcon();
        icon.setIkon(ikon);
        icon.setIconSize(ICON_SIZE);
        return icon;
    }

    public static ImageIcon getImage(Ikon ikon, Color color) {
        FontIcon icon = get(ikon, color);
        icon.setIconSize(64);
        return icon.toImageIcon();
    }

}
