package com.github.manevolent.atlas.ui.component.field;

import com.github.manevolent.atlas.model.Precision;
import com.github.manevolent.atlas.ui.util.Fonts;
import com.github.manevolent.atlas.ui.util.Inputs;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HexFormat;
import java.util.function.Consumer;

public class BinaryInputField extends JTextField implements DocumentListener {
    private static final char[] VALID_HEX_CHARACTERS = new char[]
            {'A', 'B', 'C', 'D', 'E', 'F', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    private final Precision precision;
    private final Consumer<Boolean> inputValid;

    private Double min, max;

    private long longValue;
    private double doubleValue;

    public BinaryInputField(Precision precision, double value,
                            Consumer<Boolean> inputValid,
                            Consumer<BinaryInputField> accept,
                            Runnable cancel) {
        this.inputValid = inputValid;
        this.precision = precision;
        this.doubleValue = value;
        this.longValue = Math.round(value);

        String defaultValue;
        if (precision == Precision.WHOLE_NUMBER) {
            defaultValue = "0x" + HexFormat.of().toHexDigits((int) (longValue & 0xFFFFFFFFL)).toUpperCase();
        } else {
            defaultValue = String.format("%f", value);
        }

        setText(defaultValue);
        setFont(Fonts.VALUE_FONT);
        addActionListener((e) -> accept.accept(this));
        getDocument().addDocumentListener(this);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    cancel.run();
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {
                if (e.isActionKey()) {
                    e.consume();
                    accept.accept(BinaryInputField.this);
                    return;
                }

                if (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE ||
                        e.getKeyCode() == KeyEvent.VK_KP_RIGHT || e.getKeyCode() == KeyEvent.VK_KP_LEFT) {
                    return;
                }

                if (e.getKeyChar() == 'x') {
                    if (getText().equals("0")) {
                        // This is acceptable (hex string)
                        return;
                    }
                }

                boolean selectedMultiple = getSelectionEnd() - getSelectionStart() > 1;
                if (!selectedMultiple && getText().toLowerCase().startsWith("0x")
                        && getText().length() >= 10) {
                    e.consume();
                    return;
                }

                if (!getText().startsWith("0x") && Character.toString(e.getKeyChar()).matches("[a-fA-F]")) {
                    setText("0x" + getText());
                }

                if (e.getKeyChar() == '.') {
                    if (getText().startsWith("0x")) {
                        e.consume();
                        return;
                    } else if (precision == Precision.FLOATING_POINT && !getText().contains(".")) {
                        if (getText().length() <= 0) {
                            setText("0");
                        }

                        // Acceptable in this scenario
                        return;
                    } else {
                        e.consume();
                        return;
                    }
                }

                if (e.getKeyChar() == '-') {
                    if (!getText().startsWith("0x")
                            && precision == Precision.FLOATING_POINT
                            && (min == null || min < 0)) {
                        longValue = -longValue;
                        doubleValue = -doubleValue;

                        if (getSelectionEnd() - getSelectionStart() == getText().length()) {
                            setText("-");
                        } else if (getText().startsWith("-")) {
                            setText(getText().substring(1));
                        } else {
                            setText("-" + getText());
                        }
                    }

                    e.consume();
                    return;
                }

                for (char c : VALID_HEX_CHARACTERS) {
                    if (e.getKeyChar() == c || e.getKeyChar() == Character.toLowerCase(c)) {
                        e.setKeyChar(Character.toUpperCase(c));
                        return;
                    }
                }

                e.consume();
            }
        });
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        changed();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        changed();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        changed();
    }

    private void changed() {
        String newValue = getText();

        if (newValue.isBlank()) {
            inputValid.accept(false);
            return;
        }

        try {
            if (newValue.toLowerCase().startsWith("0x")) {
                longValue = HexFormat.fromHexDigits(newValue.substring(2)) & 0xFFFFFFFFL;
                doubleValue = (double) longValue;
            } else {
                try {
                    if (precision == Precision.WHOLE_NUMBER) {
                        longValue = Long.parseLong(newValue);
                        doubleValue = (double) longValue;
                    } else {
                        doubleValue = Double.parseDouble(newValue);
                        longValue = Math.round(doubleValue);
                    }
                } catch (NumberFormatException ex) {
                    if (!newValue.isEmpty()) {
                        longValue = HexFormat.fromHexDigits(newValue);
                        doubleValue = (double) longValue;
                        SwingUtilities.invokeLater(() ->
                                setText("0x" + Integer.toHexString((int) (longValue & 0xFFFFFFFFL)).toUpperCase()));
                    }
                }
            }

            boolean hex = getText().startsWith("0x");

            boolean change;
            if (min != null && doubleValue < min) {
                doubleValue = min;
                longValue = (long) Math.ceil(doubleValue);
                change = true;
            } else if (max != null && doubleValue > max) {
                doubleValue = max;
                longValue = (long) Math.floor(doubleValue);
                change = true;
            } else {
                change = false;
            }

            if (change) {
                String value = hex ?
                        "0x" + Integer.toHexString((int) (this.longValue & 0xFFFFFFFFL)).toUpperCase() :
                        (precision == Precision.WHOLE_NUMBER ?
                                Long.toString(this.longValue) :
                                Double.toString(this.doubleValue));

                SwingUtilities.invokeLater(() -> setText(value));
            }

            inputValid.accept(true);
        } catch (Exception exception) {
            if (!getText().equals("-"))
                SwingUtilities.invokeLater(() -> setText(""));
            longValue = 0;
            doubleValue = 0.0D;
            inputValid.accept(false);
        }
    }

    public long getLongValue() {
        return longValue;
    }

    public double getDoubleValue() {
        return doubleValue;
    }

    public void setMax(double max) {
        this.max = max;
        changed();
    }

    public void setMin(double min) {
        this.min = min;
        changed();
    }
}
