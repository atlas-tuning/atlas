package com.github.manevolent.atlas.ui.dialog;

import com.github.manevolent.atlas.model.uds.SecurityAccessProperty;
import com.github.manevolent.atlas.ui.util.Fonts;
import com.github.manevolent.atlas.ui.util.Icons;
import com.github.manevolent.atlas.ui.util.Inputs;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;

public class SecurityAccessDialog extends JDialog {
    private SecurityAccessProperty property;
    private boolean canceled = false;

    private JTextField textField;

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

        textField.grabFocus();
    }

    private JTextField createKeyInputField(Consumer<Boolean> inputValid, Runnable enter) {
        textField = new JTextField(com.github.manevolent.atlas.Frame.toHexString(property.getKey()));

        textField.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                return ((JTextField)input).getText().matches("^[a-fA-F0-9]*$");
            }
        });

        textField.setFont(Fonts.VALUE_FONT);

        return textField;
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
        getRootPane().setDefaultButton(ok);

        JSpinner levelField = new JSpinner(new SpinnerNumberModel(property.getLevel(), 0, 255, 1));
        levelField.addChangeListener(e -> {
            property.setLevel((int) levelField.getValue());
        });
        JComponent editor = levelField.getEditor();
        if (editor instanceof JSpinner.DefaultEditor defaultEditor) {
            defaultEditor.getTextField().setHorizontalAlignment(JTextField.LEFT);
        }
        Inputs.createEntryRow(content, 1, "Level", "The security access level", levelField);

        JTextField keyInputField = createKeyInputField(ok::setEnabled, this::accept);
        Inputs.createEntryRow(content, 2, "Key", "The algorithm-specific key material", keyInputField);

        JButton cancel = Inputs.button("Cancel", this::cancel);
        Inputs.createButtonRow(content, 3, ok, cancel);

        getContentPane().add(content);
        keyInputField.transferFocus();
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
}
