package com.github.manevolent.atlas.ui.dialog;

import com.github.manevolent.atlas.model.DataFormat;
import com.github.manevolent.atlas.model.Precision;
import com.github.manevolent.atlas.ui.Editor;
import com.github.manevolent.atlas.ui.component.field.BinaryInputField;
import com.github.manevolent.atlas.ui.util.Icons;
import com.github.manevolent.atlas.ui.util.Inputs;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;

public class VariableInputDialog extends JDialog {
    private BinaryInputField binaryInputField;
    private final double initialValue;
    private final String message;
    private boolean canceled = false;

    public VariableInputDialog(Frame parent, String title, String message, double value) {
        super(parent, title, true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
            canceled = true;
            super.windowClosing(e);
            }
        });

        this.message = message;
        this.initialValue = value;

        setMinimumSize(new Dimension(300, getMinimumSize().height));

        setType(Type.POPUP);
        initComponent();
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
        setModal(true);
        setIconImage(Icons.getImage(CarbonIcons.STRING_INTEGER, Color.WHITE).getImage());

        binaryInputField.grabFocus();
    }

    private BinaryInputField createDataInputField(Consumer<Boolean> inputValid, Runnable enter) {
        binaryInputField = new BinaryInputField(Precision.FLOATING_POINT, initialValue,
                inputValid, (field) -> enter.run(), this::cancel);

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
        getRootPane().setDefaultButton(ok);

        BinaryInputField dataInputField = createDataInputField(ok::setEnabled, this::accept);
        Inputs.createTextRow(content, 1, message);

        Inputs.createEntryRow(content, 2, "Value", "The function coefficient",
                dataInputField);

        JButton cancel = Inputs.button("Cancel", this::cancel);
        Inputs.createButtonRow(content, 3, ok, cancel);

        getContentPane().add(content);
        dataInputField.transferFocus();
    }

    public Double getValue() {
        if (!canceled) {
            return binaryInputField.getDoubleValue();
        } else {
            return null;
        }
    }


    public static Double show(Frame parent, String title, String message, double value) {
        VariableInputDialog dialog = new VariableInputDialog(parent, title, message, value);
        dialog.setVisible(true);
        return dialog.getValue();
    }
}
