package com.github.manevolent.atlas.ui.util;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import java.awt.*;

public class Icons {
    private static final int DEFAULT_IMAGE_SIZE = 64;
    private static final int DEFAULT_ICON_SIZE = 14;


    public static FontIcon get(Ikon ikon, Color color, int size) {
        if (ikon == null) {
            return null;
        }

        FontIcon icon = new FontIcon();
        icon.setIkon(ikon);
        icon.setIconColor(color);
        icon.setIconSize(size);
        return icon;
    }

    public static FontIcon get(Ikon ikon, int size) {
        FontIcon icon = new FontIcon();
        icon.setIkon(ikon);
        icon.setIconSize(size);
        icon.setIconColor(Fonts.getTextColor());
        return icon;
    }

    public static FontIcon get(Ikon ikon, Color color) {
        return get(ikon, color, DEFAULT_ICON_SIZE);
    }

    public static FontIcon get(Ikon ikon) {
        FontIcon icon = get(ikon, DEFAULT_ICON_SIZE);
        icon.setIconColor(Fonts.getTextColor());
        return icon;
    }

    public static ImageIcon getImage(Ikon ikon, Color color, int size) {
        FontIcon icon = get(ikon, color);
        icon.setIconSize(size);
        return icon.toImageIcon();
    }

    public static ImageIcon getImage(Ikon ikon, Color color) {
        return getImage(ikon, color, DEFAULT_IMAGE_SIZE);
    }

    public static ImageIcon getImage(Ikon ikon) {
        return getImage(ikon, Fonts.getTextColor());
    }
}
