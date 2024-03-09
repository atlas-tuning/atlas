package com.github.manevolent.atlas.ui;

import java.awt.*;

public class Colors {
    public static Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }
}
