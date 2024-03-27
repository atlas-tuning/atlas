package com.github.manevolent.atlas.ui.dialog;

import com.github.manevolent.atlas.model.KeyProperty;
import com.github.manevolent.atlas.model.uds.SecurityAccessProperty;
import com.github.manevolent.atlas.ssm4.Crypto;
import com.github.manevolent.atlas.ui.util.Fonts;
import com.github.manevolent.atlas.ui.util.Icons;
import com.github.manevolent.atlas.ui.util.Inputs;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class KeyDataDialog extends JDialog implements DocumentListener {
    private JButton ok;
    private JTextField keyInputField;
    private KeyProperty property;
    private boolean canceled = false;

    public KeyDataDialog(Frame parent, KeyProperty property) {
        super(parent, "Enter Key Data", true);

        this.property = property;

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                canceled = true;
                super.windowClosing(e);
            }

            @Override
            public void windowOpened(WindowEvent e) {
                SwingUtilities.invokeLater(() -> {
                    toFront();
                    requestFocus();
                });
            }
        });


        setType(Type.POPUP);
        initComponent();
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setMinimumSize(new Dimension(300, getMinimumSize().height));
        setIconImage(Icons.getImage(CarbonIcons.PASSWORD, Color.WHITE).getImage());

        keyInputField.grabFocus();
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
        ok = Inputs.button(CarbonIcons.CHECKMARK, "OK", null, this::accept);
        getRootPane().setDefaultButton(ok);

        String text = com.github.manevolent.atlas.Frame.toHexString(property.getKey());
        if (property.getKey().length == 0) {
            text = "";
        }
        keyInputField = new JTextField(text);

        keyInputField.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                return ((JTextField)input).getText().matches("^[a-fA-F0-9]*$");
            }
        });

        keyInputField.setFont(Fonts.VALUE_FONT);

        keyInputField.getDocument().addDocumentListener(this);
        keyInputField.addActionListener(e-> this.accept());

        Inputs.createEntryRow(content, 1, "Key", "The algorithm specific key material", keyInputField);

        JButton cancel = Inputs.button("Cancel", this::cancel);
        Inputs.createButtonRow(content, 2, ok, cancel);

        getContentPane().add(content);
        keyInputField.transferFocus();
    }

    private boolean isDataValid() {
        return keyInputField.getInputVerifier().verify(keyInputField);
    }

    public KeyProperty getValue() {
        if (!canceled) {
            return property;
        } else {
            return null;
        }
    }

    public static KeyProperty show(Frame parent, KeyProperty property) {
        KeyDataDialog dialog = new KeyDataDialog(parent, property);
        dialog.toFront();
        dialog.setVisible(true);
        return dialog.getValue();
    }

    private void textUpdated() {
        boolean valid = isDataValid();

        if (valid) {
            try {
                property.setKey(Crypto.toByteArray(keyInputField.getText()));
            } catch (Exception ex) {
                valid = false;
            }
        }

        ok.setEnabled(valid);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        textUpdated();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        textUpdated();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        textUpdated();
    }

}
