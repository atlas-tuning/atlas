package com.github.manevolent.atlas.ui;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class Fonts {
    private static final String defaultConsoleFont = "Courier New";

    public static Color getTextColor() {
        return new JLabel().getForeground();
    }

    public static FontMetrics getFontMetrics(Font font) {
        Canvas c = new Canvas();
        return c.getFontMetrics(font);
    }

    public static String[] getAvailableFontFamilyNames() {
        GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        return environment.getAvailableFontFamilyNames();
    }

    public static boolean isFontAvailable(String familyName) {
        return Arrays.binarySearch(getAvailableFontFamilyNames(), familyName) >= 0;
    }

    public static String getConsoleFontFamilyName() {
        return defaultConsoleFont;
    }

    public static <T extends Component> T bold(T component) {
        component.setFont(component.getFont().deriveFont(Font.BOLD));
        return component;
    }
}
