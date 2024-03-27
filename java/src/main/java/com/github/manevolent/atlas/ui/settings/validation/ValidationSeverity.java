package com.github.manevolent.atlas.ui.settings.validation;

import org.kordamp.ikonli.Ikon;

import java.awt.*;

import static org.kordamp.ikonli.carbonicons.CarbonIcons.ERROR_FILLED;
import static org.kordamp.ikonli.carbonicons.CarbonIcons.WARNING_ALT_FILLED;

public enum ValidationSeverity {
    INFO(3, null, false, new Color(0, 0, 0, 0)),
    WARNING(2, WARNING_ALT_FILLED, false, new Color(255, 255, 0)),
    ERROR(1, ERROR_FILLED, true, new Color(255, 0, 0));

    private final int ordinal;
    private final Ikon ikon;
    private final Color color;
    private final boolean blockApply;

    ValidationSeverity(int ordinal, Ikon ikon, boolean blockApply, Color color) {
        this.ordinal = ordinal;
        this.ikon = ikon;
        this.color = color;
        this.blockApply = blockApply;
    }

    public boolean willBlockApply() {
        return blockApply;
    }

    public Color getColor() {
        return color;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public Ikon getIkon() {
        return ikon;
    }
}
