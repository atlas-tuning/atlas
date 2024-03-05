package com.github.manevolent.atlas.ui;

import java.awt.*;

public class Fonts {
    public static FontMetrics getFontMetrics(Font font) {
        Canvas c = new Canvas();
        return c.getFontMetrics(font);
    }
}
