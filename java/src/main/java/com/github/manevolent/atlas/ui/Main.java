package com.github.manevolent.atlas.ui;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.github.manevolent.atlas.model.Rom;
import com.github.manevolent.atlas.logging.Log;
import com.github.manevolent.atlas.settings.Setting;
import com.github.manevolent.atlas.settings.Settings;

import javax.swing.*;
import java.io.File;
import java.util.logging.Level;

public class Main {

    public static void main(String[] args) throws Exception {
        System.setProperty("sun.awt.exception.handler", AwtExceptionHandler.class.getName());

        SplashForm splashForm = new SplashForm();
        java.awt.EventQueue.invokeLater(() -> {
            splashForm.setVisible(true);
        });

        Thread.sleep(250L);

        splashForm.setProgress(0.1f, "Loading settings...");
        Settings.getAll();

        splashForm.setProgress(0.1f, "Loading theme...");
        FlatDarculaLaf.setup();

        splashForm.setProgress(0.2f, "Loading ROM data...");
        Rom rom;
        String lastOpenedProject = Settings.get(Setting.LAST_OPENED_PROJECT);
        File romFile = null;
        if (lastOpenedProject != null) {
            File lastOpenedProjectFile = new File(lastOpenedProject);
            if (lastOpenedProjectFile.exists()) {
                splashForm.setProgress(0.4f, "Loading " + lastOpenedProjectFile.getName() + "...");

                try {
                    rom = Rom.loadFromArchive(lastOpenedProjectFile);
                    romFile = lastOpenedProjectFile;

                    Log.ui().log(Level.INFO, "Reopened last project at " +
                            lastOpenedProjectFile.getPath() + ".");
                } catch (Exception ex) {
                    Log.ui().log(Level.SEVERE, "Problem opening last project at " +
                            lastOpenedProjectFile.getPath(), ex);
                    JOptionPane.showMessageDialog(splashForm,
                            "Failed to open project!\r\nSee console output for more details.",
                            "Open failed",
                            JOptionPane.ERROR_MESSAGE);
                    rom = Rom.builder().build();
                }
            } else {
                rom = Rom.builder().build();
                Log.ui().log(Level.WARNING, "Last opened project at " +
                        lastOpenedProjectFile.getPath() + " does not exist!");
            }
        } else {
            rom = Rom.builder().build();
            Log.ui().log(Level.INFO, "Opened a new project.");
        }


        splashForm.setProgress(0.5f, "Initializing UI...");
        EditorForm editorForm = new EditorForm(rom);

        splashForm.setProgress(0.75f, "Opening ROM...");
        editorForm.openRom(romFile, rom);

        splashForm.setProgress(1.0f, "Opening Atlas...");
        Thread.sleep(500L);
        splashForm.dispose();

        java.awt.EventQueue.invokeLater(() -> {
            editorForm.setVisible(true);
            Log.get().log(Level.FINE, "Application started.");
        });
    }

}
