package com.github.manevolent.atlas.ui.dialog.settings.element;

import com.github.manevolent.atlas.ui.util.Inputs;

import javax.swing.*;
import java.util.function.Consumer;

public class TextSettingField extends AbstractSettingField<String> {
    private final Consumer<String> apply;
    private final JTextField textField;

    public TextSettingField(String name,
                            String tooltip,
                            String defaultValue,
                            Consumer<String> apply) {
        super(name, tooltip);

        this.apply = apply;
        this.textField = Inputs.textField(defaultValue, (text) -> { /*ignore*/ });
    }

    @Override
    public JComponent getInputComponent() {
        return textField;
    }

    @Override
    public String apply() {
        String value = textField.getText();
        apply.accept(value);
        return value;
    }
}