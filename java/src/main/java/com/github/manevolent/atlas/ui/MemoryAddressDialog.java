package com.github.manevolent.atlas.ui;

import com.github.manevolent.atlas.model.MemoryAddress;
import com.github.manevolent.atlas.model.MemorySection;
import com.github.manevolent.atlas.model.Rom;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import java.util.HexFormat;
import java.util.function.Consumer;

import static com.github.manevolent.atlas.ui.Inputs.memorySectionField;

public class MemoryAddressDialog extends JDialog {
    private final Rom rom;
    private final Consumer<MemoryAddress> valueChanged;
    private MemorySection section;
    private long offset;
    private static final char[] VALID_HEX_CHARACTERS = new char[]
            {'A', 'B', 'C', 'D', 'E', 'F', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    private JTextField memoryAddressField;

    private final boolean localOnly;

    public MemoryAddressDialog(Rom rom, MemoryAddress address, Frame parent,
                               boolean localOnly, Consumer<MemoryAddress> valueChanged) {
        super(parent, "Enter Address", true);

        this.localOnly = localOnly;

        this.rom = rom;
        this.valueChanged = valueChanged;

        this.section = address != null ? address.getSection() : getDefaultSection();
        this.offset = address != null ? address.getOffset() : section.getBaseAddress();

        setType(Type.POPUP);
        initComponent();
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
        setMinimumSize(new Dimension(300, getMinimumSize().height));
        memoryAddressField.grabFocus();
    }

    private MemorySection getDefaultSection() {
        return rom.getSections().getFirst();
    }

    private JTextField createMemoryAddressField(Consumer<Boolean> inputValid, Runnable enter) {
        String defaultValue = "0x" + HexFormat.of().toHexDigits((int) (offset & 0xFFFFFFFF)).toUpperCase();
        memoryAddressField = Inputs.textField(defaultValue, (newValue) -> {
            if (newValue.isBlank()) {
                inputValid.accept(false);
                return;
            }

            try {
                if (newValue.toLowerCase().startsWith("0x")) {
                    offset = HexFormat.fromHexDigits(newValue.substring(2));

                    if (offset < section.getBaseAddress()) {
                        inputValid.accept(false);
                        return;
                    } else if (offset > section.getBaseAddress() + section.getDataLength()) {
                        offset = section.getBaseAddress() + section.getDataLength() - 1;
                        SwingUtilities.invokeLater(() -> memoryAddressField.setText("0x" + HexFormat.of().toHexDigits(
                                (int) (offset)).toUpperCase()));
                    }
                } else {
                    try {
                        offset = (int) Long.parseLong(newValue);
                    } catch (NumberFormatException ex) {
                        if (!newValue.isEmpty()) {
                            offset = HexFormat.fromHexDigits(newValue);
                            SwingUtilities.invokeLater(() -> memoryAddressField.setText("0x" +
                                    Integer.toHexString((int) (offset)).toUpperCase()));
                        }
                    }

                    if (offset < section.getBaseAddress()) {
                        inputValid.accept(false);
                        return;
                    } else if (offset > section.getBaseAddress() + section.getDataLength()) {
                        offset = section.getBaseAddress() + section.getDataLength() - 1;
                        SwingUtilities.invokeLater(() -> memoryAddressField.setText(Long.toString(offset)));
                    }
                }

                inputValid.accept(true);
            } catch (Exception exception) {
                SwingUtilities.invokeLater(() -> {
                    memoryAddressField.setText("");
                });
                inputValid.accept(false);
            }
        });
        memoryAddressField.setFont(Fonts.VALUE_FONT);
        memoryAddressField.addActionListener((e) -> {
            accept();
        });
        memoryAddressField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    cancel();
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {
                if (e.isActionKey()) {
                    e.consume();
                    accept();
                    return;
                }

                if (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE ||
                    e.getKeyCode() == KeyEvent.VK_KP_RIGHT || e.getKeyCode() == KeyEvent.VK_KP_LEFT) {
                    return;
                }

                if (e.getKeyChar() == 'x') {
                    if (memoryAddressField.getText().equals("0")) {
                        // This is acceptable (hex string)
                        return;
                    }
                }

                boolean selectedMultiple = memoryAddressField.getSelectionEnd()
                        - memoryAddressField.getSelectionStart() > 1;
                if (!selectedMultiple && memoryAddressField.getText().toLowerCase().startsWith("0x")
                        && memoryAddressField.getText().length() >= 10) {
                    e.consume();
                    return;
                }

                if (!memoryAddressField.getText().startsWith("0x") &&
                        Character.toString(e.getKeyChar()).matches("[a-fA-F]")) {
                    memoryAddressField.setText("0x" + memoryAddressField.getText());
                }

                char[] valid = VALID_HEX_CHARACTERS;
                for (int i = 0; i < valid.length; i ++) {
                    if (e.getKeyChar() == valid[i] ||
                        e.getKeyChar() == Character.toLowerCase(valid[i])) {
                        e.setKeyChar(Character.toUpperCase(valid[i]));
                        return;
                    }
                }

                e.consume();
            }
        });
        return memoryAddressField;
    }

    private void accept() {
        MemoryAddress address = MemoryAddress.builder().withOffset(offset).withSection(section).build();
        valueChanged.accept(address);
        dispose();
    }

    private void cancel() {
        dispose();
    }

    private void initComponent() {
        JPanel content = Inputs.createEntryPanel();
        JButton ok = Inputs.button(CarbonIcons.CHECKMARK, "OK", null, this::accept);


        JTextField addressField = createMemoryAddressField(ok::setEnabled, this::accept);
        Inputs.createEntryRow(content, 2, "Address", "The memory address relative to the selected ROM region",
                addressField);

        Inputs.createEntryRow(content, 1, "Region", "The ROM section this address will reside in",
                memorySectionField(rom, section, localOnly, (newSection) -> {
                    this.section = newSection;
                    addressField.setText("0x" + HexFormat.of().toHexDigits(
                            (int) (section.getBaseAddress() & 0xFFFFFFFF)).toUpperCase());
                }));

        JButton cancel = Inputs.button("Cancel", this::cancel);
        Inputs.createButtonRow(content, 3, cancel, ok);

        getContentPane().add(content);
        addressField.transferFocus();
    }

    public MemoryAddress getAddress() {
        return MemoryAddress.builder()
                .withSection(section)
                .build();
    }

    public static void show(Frame parent, Rom rom, MemoryAddress current,
                            boolean localOnly, Consumer<MemoryAddress> changed) {
        MemoryAddressDialog dialog = new MemoryAddressDialog(rom, current, parent, localOnly, changed);
        dialog.setVisible(true);
    }
}
