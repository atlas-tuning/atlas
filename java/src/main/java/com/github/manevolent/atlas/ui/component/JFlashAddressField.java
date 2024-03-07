package com.github.manevolent.atlas.ui.component;

import com.github.manevolent.atlas.definition.*;
import com.github.manevolent.atlas.ui.Inputs;
import com.github.manevolent.atlas.ui.window.MemoryAddressDialog;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class JFlashAddressField extends JPanel {
    private final Rom rom;

    private JButton selectButton;

    private MemoryAddress address;

    public JFlashAddressField(Rom rom, Table table, Axis axis, Consumer<MemoryAddress> changed) {
        this.rom = rom;

        Supplier<String> defaultValue = () -> address.toString();

        setLayout(new BorderLayout());

        // Set default values
        Series series = axis == null ? table.getData() : table.getSeries(axis);
        address = series != null ? series.getAddress() : null;
        if (address == null) {
            address = getDefaultAddress();
        }

        JTextField textField = Inputs.textField(
                defaultValue.get(),
                "The data address for this series",
                false,
                (newValue) -> { /* ignore */ }
        );

        textField.setFocusable(false);

        add(textField, BorderLayout.CENTER);

        selectButton = Inputs.button(
                CarbonIcons.DATA_REFERENCE,
                new JLabel().getForeground(),
                ()  -> {
                    MemoryAddressDialog.show(null, rom, series != null ? series.getAddress() : getDefaultAddress());
                    changed.accept(getDataAddress());
                });

        if (axis != null) {
            selectButton.setToolTipText("Select region...");
        } else {
            selectButton.setToolTipText("Select address...");
        }

        selectButton.setFocusable(false);

        add(selectButton, BorderLayout.EAST);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        selectButton.setEnabled(enabled);
    }

    public MemoryAddress getDefaultAddress() {
        return MemoryAddress.builder()
                .withSection(rom.getSections().getFirst()) // First region available
                .withOffset(0x00000000)
                .build();
    }

    public MemoryAddress getDataAddress() {
        return address;
    }
}
