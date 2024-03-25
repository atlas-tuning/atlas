package com.github.manevolent.atlas.ui.dialog;

import com.github.manevolent.atlas.model.ArithmeticOperation;
import com.github.manevolent.atlas.model.DataFormat;
import com.github.manevolent.atlas.model.Precision;
import com.github.manevolent.atlas.ui.Editor;
import com.github.manevolent.atlas.ui.component.field.BinaryInputField;
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
    private final long minValue, maxValue;
    private static final char[] VALID_HEX_CHARACTERS = new char[]
            {'A', 'B', 'C', 'D', 'E', 'F', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    private BinaryInputField binaryInputField;

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

    private BinaryInputField createDataInputField(Consumer<Boolean> inputValid, Runnable enter) {
        binaryInputField = new BinaryInputField(Precision.WHOLE_NUMBER, 0D,
                inputValid, (field) -> enter.run(), this::cancel);

        binaryInputField.setMin(minValue);
        binaryInputField.setMax(maxValue);

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

        BinaryInputField dataInputField = createDataInputField(ok::setEnabled, this::accept);
        Inputs.createEntryRow(content, 1, "Value", "The data value",
                dataInputField);

        JButton cancel = Inputs.button("Cancel", this::cancel);
        Inputs.createButtonRow(content, 2, ok, cancel);

        getContentPane().add(content);
        dataInputField.transferFocus();
    }

    public Long getValue() {
        if (!canceled) {
            return binaryInputField.getLongValue();
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
