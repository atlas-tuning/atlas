package com.github.manevolent.atlas.ui.dialog.settings.field;

import com.github.manevolent.atlas.ui.component.field.AddressField;

import javax.swing.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class AddressSettingField extends AbstractSettingField<String> {
    private final Function<Long, Boolean> apply;
    private final AddressField binaryInputField;
    private boolean dirty;

    public AddressSettingField(String name,
                               String tooltip,
                               long defaultValue,
                               Function<Long, Boolean> apply,
                               Consumer<Long> change) {
        super(name, tooltip);

        this.apply = apply;
        this.binaryInputField = new AddressField(defaultValue, var -> {
            change.accept(var);
            dirty = true;
        });
    }

    @Override
    public JComponent getInputComponent() {
        return binaryInputField;
    }

    @Override
    public boolean apply() {
        long value = binaryInputField.getValue();
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