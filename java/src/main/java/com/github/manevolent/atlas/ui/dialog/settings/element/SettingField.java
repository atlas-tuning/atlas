package com.github.manevolent.atlas.ui.dialog.settings.element;

import javax.swing.*;

public interface SettingField<V> {
    String getName();
    String getTooltip();
    JComponent getInputComponent();
    boolean apply();
}