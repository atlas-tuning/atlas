package com.github.manevolent.atlas.ui.dialog;

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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SecurityAccessDialog extends JDialog implements DocumentListener {
    private JButton ok;
    private JTextField keyInputField;
    private JSpinner levelField;
    private SecurityAccessProperty property;
    private boolean canceled = false;

    public SecurityAccessDialog(Frame parent, SecurityAccessProperty property) {
        super(parent, "Enter Security Access Information", true);

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

        levelField = new JSpinner(new SpinnerNumberModel(property.getLevel(), 0, 255, 1));
        levelField.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                JSpinner.NumberEditor editor = (JSpinner.NumberEditor) ((JSpinner)input).getEditor();
                JTextField textField = editor.getTextField();
                return textField.getText().matches("^[0-9]+$");
            }
        });

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
        keyInputField.addActionListener( e-> this.accept());

        levelField.addChangeListener(e -> {
            ok.setEnabled(isDataValid());
            property.setLevel((int) levelField.getValue());
        });

        JComponent editor = levelField.getEditor();

        if (editor instanceof JSpinner.DefaultEditor defaultEditor) {
            ((JSpinner.DefaultEditor) editor).getTextField().getDocument().addDocumentListener(this);
            defaultEditor.getTextField().setHorizontalAlignment(JTextField.LEFT);
        }

        Inputs.createEntryRow(content, 1, "Level", "The security access level", levelField);

        Inputs.createEntryRow(content, 2, "Key", "The algorithm specific key material", keyInputField);

        JButton cancel = Inputs.button("Cancel", this::cancel);
        Inputs.createButtonRow(content, 3, ok, cancel);

        getContentPane().add(content);
        keyInputField.transferFocus();
    }

    private boolean isDataValid() {
        return levelField.getInputVerifier().verify(levelField) &&
                keyInputField.getInputVerifier().verify(keyInputField);
    }

    public SecurityAccessProperty getValue() {
        if (!canceled) {
            return property;
        } else {
            return null;
        }
    }

    public static SecurityAccessProperty show(Frame parent, SecurityAccessProperty property) {
        SecurityAccessDialog dialog = new SecurityAccessDialog(parent, property);
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
