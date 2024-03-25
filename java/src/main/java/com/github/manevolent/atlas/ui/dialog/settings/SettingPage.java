package com.github.manevolent.atlas.ui.dialog.settings;

import org.kordamp.ikonli.Ikon;

import javax.swing.*;

public interface SettingPage {

    String getName();

    Ikon getIcon();

    JComponent getContent();

    void apply();

}
