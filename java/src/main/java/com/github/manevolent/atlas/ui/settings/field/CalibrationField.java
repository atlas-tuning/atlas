package com.github.manevolent.atlas.ui.settings.field;

import com.github.manevolent.atlas.logging.Log;
import com.github.manevolent.atlas.model.Calibration;
import com.github.manevolent.atlas.ui.util.Inputs;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.util.logging.Level;

public class CalibrationField extends AbstractSettingField<byte[]> {
    private final Calibration calibration;

    private boolean dirty;
    private final JPanel buttonRow;

    public CalibrationField(String name, Calibration calibration, Runnable changed) {
        super(name, null);
        this.calibration = calibration;

        buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT));

        buttonRow.add(Inputs.button(CarbonIcons.FOLDER, "Open ROM...", () -> {
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter def = new FileNameExtensionFilter("Binary files", "bin");
            fileChooser.addChoosableFileFilter(def);
            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("ROM files", "rom"));
            fileChooser.setFileFilter(def);
            fileChooser.setDialogTitle("Open ROM file");
            if (fileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File file = fileChooser.getSelectedFile();
            try (FileInputStream inputStream = new FileInputStream(file)) {
                byte[] data = inputStream.readAllBytes();
                calibration.updateSource(data);
            } catch (Exception e) {
                Log.can().log(Level.SEVERE, "Problem opening ROM file " + file.getAbsolutePath(), e);
                JOptionPane.showMessageDialog(null, "Problem opening ROM file!\r\n" +
                                e.getMessage() + "\r\n" + "See console output (F12) for more details.",
                        "Open failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        }));

        buttonRow.add(Inputs.button(CarbonIcons.EXPORT, "Export ROM...", () -> {

        }));
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
