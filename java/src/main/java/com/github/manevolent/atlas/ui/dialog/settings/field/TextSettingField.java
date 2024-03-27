package com.github.manevolent.atlas.ui.dialog.settings.field;

import com.github.manevolent.atlas.ui.util.Inputs;

import javax.swing.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class TextSettingField extends AbstractSettingField<String> {
    private final Function<String, Boolean> apply;
    private final JTextField textField;

    private boolean dirty;

    public TextSettingField(String name,
                            String tooltip,
                            String defaultValue,
                            Function<String, Boolean> apply,
                            Consumer<String> changed) {
        super(name, tooltip);

        this.apply = apply;
        this.textField = Inputs.textField(defaultValue, (text) -> {
            changed.accept(text);
            dirty = true;
        });
    }

    @Override
    public JComponent getInputComponent() {
        return textField;
    }

    @Override
    public boolean apply() {
        String value = textField.getText();
        boolean applied = apply.apply(value);
        if (applied) {
            dirty = false;
        }
        return applied;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }
}