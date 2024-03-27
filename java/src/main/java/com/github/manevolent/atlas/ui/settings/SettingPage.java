package com.github.manevolent.atlas.ui.settings;

import com.github.manevolent.atlas.ui.settings.validation.ValidationState;
import org.kordamp.ikonli.Ikon;

import javax.swing.*;

public interface SettingPage {

    String getName();

    Ikon getIcon();

    JComponent getContent();

    boolean apply();


    default ValidationState validate() {
        ValidationState state = new ValidationState();
        validate(state);
        return state;
    }

    default void validate(ValidationState state) {

    }

    default boolean isScrollNeeded() {
        return true;
    }

    boolean isDirty();

    default void focus() {

    }
}
