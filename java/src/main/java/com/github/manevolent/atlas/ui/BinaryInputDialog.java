package com.github.manevolent.atlas.ui;

import com.github.manevolent.atlas.model.DataFormat;
import com.github.manevolent.atlas.model.MemorySection;
import com.github.manevolent.atlas.model.Precision;
import com.github.manevolent.atlas.ui.util.Fonts;
import com.github.manevolent.atlas.ui.util.Inputs;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HexFormat;
import java.util.function.Consumer;

import static com.github.manevolent.atlas.ui.util.Inputs.memorySectionField;

public class BinaryInputDialog extends JDialog {
    private MemorySection section;
    private final long minValue, maxValue;
    private long value;
    private static final char[] VALID_HEX_CHARACTERS = new char[]
            {'A', 'B', 'C', 'D', 'E', 'F', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    private JTextField binaryInputField;

    private JComboBox<MemorySection> sectionField;
    private boolean canceled = false;

    public BinaryInputDialog(Frame parent, long minValue, long maxValue) {
        super(parent, "Enter Data Value", true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
            canceled = true;
            super.windowClosing(e);
            }
        });

        this.minValue = minValue;
        this.maxValue = maxValue;

        setType(Type.POPUP);
        initComponent();
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
        setModal(true);
        setMinimumSize(new Dimension(300, getMinimumSize().height));

        binaryInputField.grabFocus();
    }

    private JTextField createDataInputField(Consumer<Boolean> inputValid, Runnable enter) {
        String defaultValue = "0x" + HexFormat.of().toHexDigits((int) (value & 0xFFFFFFFFL)).toUpperCase();
        binaryInputField = Inputs.textField(defaultValue, (newValue) -> {
            if (newValue.isBlank()) {
                inputValid.accept(false);
                return;
            }

            try {
                boolean hex = false;
                if (newValue.toLowerCase().startsWith("0x")) {
                    value = HexFormat.fromHexDigits(newValue.substring(2)) & 0xFFFFFFFFL;
                    hex = true;
                } else {
                    try {
                        value = Long.parseLong(newValue);
                    } catch (NumberFormatException ex) {
                        if (!newValue.isEmpty()) {
                            value = HexFormat.fromHexDigits(newValue);
                            hex = true;
                            SwingUtilities.invokeLater(() -> binaryInputField.setText("0x" +
                                    Integer.toHexString((int) (value)).toUpperCase()));
                        }
                    }
                }

                boolean change;
                if (value < minValue) {
                    value = maxValue;
                    change = true;
                } else if (value > maxValue) {
                    value = maxValue;
                    change = true;
                } else {
                    change = false;
                }

                if (change) {
                    String value = hex ?
                            "0x" + Integer.toHexString((int) (this.value)).toUpperCase() :
                            Long.toString(this.value);
                    SwingUtilities.invokeLater(() -> binaryInputField.setText(value));
                }

                inputValid.accept(true);
            } catch (Exception exception) {
                SwingUtilities.invokeLater(() -> {
                    binaryInputField.setText("");
                });
                inputValid.accept(false);
            }
        });

        binaryInputField.setFont(Fonts.VALUE_FONT);
        binaryInputField.addActionListener((e) -> {
            accept();
        });

        binaryInputField.addKeyListener(new KeyAdapter() {
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
                    if (binaryInputField.getText().equals("0")) {
                        // This is acceptable (hex string)
                        return;
                    }
                }

                boolean selectedMultiple = binaryInputField.getSelectionEnd()
                        - binaryInputField.getSelectionStart() > 1;
                if (!selectedMultiple && binaryInputField.getText().toLowerCase().startsWith("0x")
                        && binaryInputField.getText().length() >= 10) {
                    e.consume();
                    return;
                }

                if (!binaryInputField.getText().startsWith("0x") &&
                        Character.toString(e.getKeyChar()).matches("[a-fA-F]")) {
                    binaryInputField.setText("0x" + binaryInputField.getText());
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
        return binaryInputField;
    }

    private void accept() {
        dispose();
        canceled = false;
    }

    private void cancel() {
        canceled = true;
        dispose();
    }

    private void initComponent() {
        JPanel content = Inputs.createEntryPanel();
        JButton ok = Inputs.button(CarbonIcons.CHECKMARK, "OK", null, this::accept);

        JTextField dataInputField = createDataInputField(ok::setEnabled, this::accept);
        Inputs.createEntryRow(content, 1, "Value", "The data value",
                dataInputField);

        JButton cancel = Inputs.button("Cancel", this::cancel);
        Inputs.createButtonRow(content, 3, ok, cancel);

        getContentPane().add(content);
        dataInputField.transferFocus();
    }

    public Long getValue() {
        if (!canceled) {
            return value;
        } else {
            return null;
        }
    }


    public static Long show(Editor parent, DataFormat format) {
        return show(parent, (long)format.getMin(), (long)format.getMax());
    }

    public static Long show(Frame parent, long minValue, long maxValue) {
        BinaryInputDialog dialog = new BinaryInputDialog(parent, minValue, maxValue);
        dialog.setVisible(true);
        return dialog.getValue();
    }
}
