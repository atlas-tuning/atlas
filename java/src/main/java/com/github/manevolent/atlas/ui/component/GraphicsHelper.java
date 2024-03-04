package com.github.manevolent.atlas.ui.component;

import java.awt.*;

public class GraphicsHelper {
    public static FontMetrics getFontMetrics(Font font) {
        Canvas c = new Canvas();
        return c.getFontMetrics(font);
    }
}
