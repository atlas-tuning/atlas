package com.github.manevolent.atlas.ui.component;

import com.github.manevolent.atlas.definition.*;
import com.github.manevolent.atlas.ui.Inputs;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class JFlashRegionField extends JPanel {
    private final Rom rom;
    private final Table table;
    private final Axis axis;

    private JButton selectButton;

    private FlashAddress address;
    private int dataLength;

    public JFlashRegionField(Rom rom, Table table, Axis axis, BiConsumer<FlashAddress, Integer> changed) {
        this.rom = rom;
        this.table = table;
        this.axis = axis;

        Supplier<String> defaultValue = () -> {
            if (axis != null) {
                return address.toString() + ":" + dataLength;
            } else {
                return address.toString();
            }
        };

        setLayout(new BorderLayout());

        // Set default values
        Series series = axis == null ? table.getData() : table.getSeries(axis);
        address = series != null ? series.getAddress() : null;
        if (address == null) {
            address = getDefaultAddress();
        }
        dataLength = series != null ? series.getLength() : 1;

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
                    //TODO open flash selection dialog
                    changed.accept(getDataAddress(), getDataLength());
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

    public FlashAddress getDefaultAddress() {
        return FlashAddress.builder()
                .withRegion(rom.getRegions().getFirst()) // First region available
                .withOffset(0x00000000)
                .build();
    }

    public FlashAddress getDataAddress() {
        return address;
    }

    public int getDataLength() {
        return dataLength;
    }
}
