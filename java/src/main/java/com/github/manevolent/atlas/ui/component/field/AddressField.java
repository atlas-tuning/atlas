package com.github.manevolent.atlas.ui.component.field;

import com.github.manevolent.atlas.Frame;
import com.github.manevolent.atlas.ui.dialog.BinaryInputDialog;

import com.github.manevolent.atlas.ui.util.Inputs;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.ByteBuffer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AddressField extends JPanel {
    private final JTextField textField;
    private final JButton selectButton;
    private long value = 0L;

    public AddressField(long existing, Consumer<Long> changed) {
        Supplier<String> formatValue = () -> {
            byte[] data = ByteBuffer.allocate(4).putInt((int) (value & 0xFFFFFFFFL)).array();;
            return "0x" + Frame.toHexString(data);
        };

        setLayout(new BorderLayout());

        value = existing;

        textField = Inputs.textField(
                formatValue.get(),
                "The binary data for this field",
                false,
                (newValue) -> { /* ignore */ }
        );

        Runnable set = () ->{
            Long value = BinaryInputDialog.show(
                    null,
                    this.value,
                    0,
                    0xFFFFFFFFL
            );

            if (value != null) {
                this.value = value;
                textField.setText(formatValue.get());
                changed.accept(value);
            }
        };

        textField.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        textField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (AddressField.this.isEnabled()) {
                    set.run();
                }
            }
        });

        textField.setFocusable(false);

        add(textField, BorderLayout.CENTER);

        selectButton = Inputs.button(
                CarbonIcons.DATA_REFERENCE,
                new JLabel().getForeground(),
                set
        );

        selectButton.setToolTipText("Enter data...");

        selectButton.setFocusable(false);

        add(selectButton, BorderLayout.EAST);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        selectButton.setEnabled(enabled);
    }

    public long getValue() {
        return value;
    }
}
