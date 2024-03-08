package com.github.manevolent.atlas.ui.component;

import com.github.manevolent.atlas.model.*;
import com.github.manevolent.atlas.ui.Inputs;
import com.github.manevolent.atlas.ui.window.MemoryAddressDialog;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MemoryAddressField extends JPanel {
    private final Rom rom;

    private JTextField textField;
    private JButton selectButton;

    private MemoryAddress address;

    public MemoryAddressField(Rom rom, MemoryAddress existing, boolean localOnly, Consumer<MemoryAddress> changed) {
        this.rom = rom;

        Supplier<String> defaultValue = () -> address.toString();

        setLayout(new BorderLayout());

        // Set default values
        address = existing;
        if (address == null) {
            address = getDefaultAddress();
        }

        textField = Inputs.textField(
                defaultValue.get(),
                "The data address for this series",
                false,
                (newValue) -> { /* ignore */ }
        );

        Runnable set = () -> MemoryAddressDialog.show(
                null,
                rom,
                existing,
                localOnly,
                (newValue) -> {
                    textField.setText(newValue.toString());
                    changed.accept(newValue);
                }
        );

        textField.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        textField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (MemoryAddressField.this.isEnabled()) {
                    set.run();
                }
            }
        });

        textField.setFocusable(false);

        add(textField, BorderLayout.CENTER);

        selectButton = Inputs.button(
                CarbonIcons.DATA_REFERENCE,
                new JLabel().getForeground(),
                set);

        selectButton.setToolTipText("Select address...");

        selectButton.setFocusable(false);

        add(selectButton, BorderLayout.EAST);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        selectButton.setEnabled(enabled);
    }

    public MemoryAddress getDefaultAddress() {
        MemorySection section = rom.getSections().getFirst();
        return MemoryAddress.builder()
                .withSection(section) // First region available
                .withOffset(section.getBaseAddress())
                .build();
    }

    public MemoryAddress getDataAddress() {
        return address;
    }
}
