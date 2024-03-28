package com.github.manevolent.atlas.ui.settings.field;

import com.github.manevolent.atlas.logging.Log;
import com.github.manevolent.atlas.model.Calibration;
import com.github.manevolent.atlas.ui.dialog.BinaryInputDialog;
import com.github.manevolent.atlas.ui.util.Inputs;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.util.logging.Level;

import static javax.swing.JOptionPane.WARNING_MESSAGE;

public class CalibrationField extends AbstractSettingField<byte[]> {
    private final Calibration calibration;

    private boolean dirty;
    private JButton export;
    private final JPanel buttonRow;

    public CalibrationField(String name, Calibration calibration, Runnable changed) {
        super(name, null);
        this.calibration = calibration;

        buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT));

        buttonRow.add(Inputs.button(CarbonIcons.FOLDER, "Open ROM...", () -> {
            if (calibration.getSection() == null) {
                return;
            }

            if (calibration.hasData()) {
                if (JOptionPane.showConfirmDialog(null,
                        "WARNING!\r\n" + calibration.getName() + " already has ROM binary data.\r\n" +
                                "Changing this data will PERMANENTLY remove any table data associated with this calibration in Atlas.\r\n" +
                                "Proceed with replacing existing data with a new ROM file?",
                        "Overwrite Calibration",
                        JOptionPane.YES_NO_OPTION,
                        WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter def = new FileNameExtensionFilter("Binary files", "bin");
            fileChooser.addChoosableFileFilter(def);
            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("ROM files", "rom"));
            fileChooser.setFileFilter(def);
            fileChooser.setDialogTitle("Open ROM file - " + calibration.getName());
            if (fileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File file = fileChooser.getSelectedFile();
            byte[] data;
            try (FileInputStream inputStream = new FileInputStream(file)) {
                data = inputStream.readAllBytes();
            } catch (Exception e) {
                Log.can().log(Level.SEVERE, "Problem opening ROM file " + file.getAbsolutePath(), e);
                JOptionPane.showMessageDialog(null, "Problem opening ROM file!\r\n" +
                                e.getMessage() + "\r\n" + "See console output (F12) for more details.",
                        "Open ROM failed",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            int offset = 0x0;
            int length = calibration.getSection().getDataLength();

            if (data.length < calibration.getSection().getDataLength()) {
                JOptionPane.showMessageDialog(null,
                        "The ROM file you provided is too short: it is " +
                                data.length + " bytes long, but the " +
                                calibration.getSection().getName() + " memory region is " +
                                calibration.getSection().getDataLength() + " bytes long.",
                        "Open ROM failed",
                        JOptionPane.ERROR_MESSAGE);
                return;
            } else if (data.length > calibration.getSection().getDataLength()) {
                if (JOptionPane.showConfirmDialog(null,
                        "The ROM file you provided is " + data.length + " bytes long, but the " +
                        calibration.getSection().getName() + " memory region is " +
                                calibration.getSection().getDataLength() + " bytes long. " +
                        "An byte offset in \"" + file.getName() + "\" will be required to inform Atlas" +
                                " where the corresponding start of the memory region is located.\r\nWould you like to provide an offset?",
                        "Offset Required",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE) != JOptionPane.OK_OPTION) {
                    return;
                }

                Long answer = BinaryInputDialog.show(null,
                        calibration.getSection().getBaseAddress(),
                        0, 0xFFFFFFFFL);

                if (answer == null) {
                    return;
                }

                offset = (int) (long) answer;
            }

            calibration.updateSource(data, offset, length);

            export.setEnabled(calibration.hasData());
        }));

        buttonRow.add(export = Inputs.button(CarbonIcons.EXPORT, "Export ROM...", () -> {

        }));

        export.setEnabled(calibration.hasData());
    }

    @Override
    public JComponent getInputComponent() {
        return buttonRow;
    }

    @Override
    public boolean apply() {
        return true;
    }

    @Override
    public boolean isDirty() {
        return false;
    }
}
