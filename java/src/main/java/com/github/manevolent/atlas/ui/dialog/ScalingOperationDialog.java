package com.github.manevolent.atlas.ui.dialog;

import com.github.manevolent.atlas.model.ArithmeticOperation;
import com.github.manevolent.atlas.model.Precision;
import com.github.manevolent.atlas.model.ScalingOperation;
import com.github.manevolent.atlas.ui.component.field.BinaryInputField;
import com.github.manevolent.atlas.ui.util.Inputs;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;

public class ScalingOperationDialog extends JDialog {
    private ScalingOperation value;

    private BinaryInputField binaryInputField;
    private JComboBox<ArithmeticOperation> operationField;
    private boolean canceled = false;

    public ScalingOperationDialog(Frame parent, ScalingOperation value) {
        super(parent, "Enter Scaling Operation", true);

        this.value = value;

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
            canceled = true;
            super.windowClosing(e);
            }
        });

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
        binaryInputField = new BinaryInputField(Precision.FLOATING_POINT, value.getCoefficient(),
                inputValid, (field) -> enter.run(), this::cancel);

        return binaryInputField;
    }

    private void accept() {
        value.setCoefficient((float) binaryInputField.getDoubleValue());
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

        operationField = Inputs.arithmeticOperationField(
                "The arithmetic operation to perform",
                value,
                (op) -> value.setOperation(op)
        );
        Inputs.createEntryRow(content, 1, "Operation", "The arithmetic operation to perform", operationField);

        JTextField dataInputField = createDataInputField(ok::setEnabled, this::accept);
        Inputs.createEntryRow(content, 2, "Value", "The data value", dataInputField);

        JButton cancel = Inputs.button("Cancel", this::cancel);
        Inputs.createButtonRow(content, 3, ok, cancel);

        getContentPane().add(content);
        dataInputField.transferFocus();
    }

    public ScalingOperation getValue() {
        if (!canceled) {
            return value;
        } else {
            return null;
        }
    }

    public static ScalingOperation show(Frame parent, ScalingOperation existing) {
        ScalingOperationDialog dialog = new ScalingOperationDialog(parent, existing);
        dialog.setVisible(true);
        return dialog.getValue();
    }

    public static ScalingOperation show(Frame parent) {
        ScalingOperation scalingOperation = new ScalingOperation();
        scalingOperation.setOperation(ArithmeticOperation.ADD);
        scalingOperation.setCoefficient(0.0f);
        return show(parent, scalingOperation);
    }
}
