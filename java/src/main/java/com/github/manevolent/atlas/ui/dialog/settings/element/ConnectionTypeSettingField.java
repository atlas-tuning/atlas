package com.github.manevolent.atlas.ui.dialog.settings.element;

import com.github.manevolent.atlas.connection.ConnectionType;
import com.github.manevolent.atlas.ui.util.Inputs;

import javax.swing.*;
import java.util.function.Consumer;

public class ConnectionTypeSettingField extends AbstractSettingField<ConnectionType> {
    private final Consumer<ConnectionType> apply;
    private final JComboBox<ConnectionType> comboBox;

    public ConnectionTypeSettingField(String name,
                                      String tooltip,
                                      ConnectionType defaultValue,
                                      Consumer<ConnectionType> apply) {
        super(name, tooltip);

        this.apply = apply;
        this.comboBox = Inputs.connectionTypeField(tooltip, defaultValue, (text) -> { /*ignore*/ });
    }

    @Override
    public JComponent getInputComponent() {
        return comboBox;
    }

    @Override
    public ConnectionType apply() {
        ConnectionType value = (ConnectionType) comboBox.getSelectedItem();
        apply.accept(value);
        return value;
    }
}