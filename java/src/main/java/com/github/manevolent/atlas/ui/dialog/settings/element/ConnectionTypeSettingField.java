package com.github.manevolent.atlas.ui.dialog.settings.element;

import com.github.manevolent.atlas.connection.ConnectionType;
import com.github.manevolent.atlas.ui.util.Inputs;

import javax.swing.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class ConnectionTypeSettingField extends AbstractSettingField<ConnectionType> {
    private final Function<ConnectionType, Boolean> apply;
    private final JComboBox<ConnectionType> comboBox;

    public ConnectionTypeSettingField(String name,
                                      String tooltip,
                                      ConnectionType defaultValue,
                                      Function<ConnectionType, Boolean> apply,
                                      Consumer<ConnectionType> changed) {
        super(name, tooltip);

        this.apply = apply;
        this.comboBox = Inputs.connectionTypeField(tooltip, defaultValue, changed);
    }

    @Override
    public JComponent getInputComponent() {
        return comboBox;
    }

    @Override
    public boolean apply() {
        ConnectionType value = (ConnectionType) comboBox.getSelectedItem();
        return apply.apply(value);
    }
}