package com.github.manevolent.atlas.ui;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.github.manevolent.atlas.model.Project;
import com.github.manevolent.atlas.logging.Log;
import com.github.manevolent.atlas.settings.Setting;
import com.github.manevolent.atlas.settings.Settings;

import javax.swing.*;
import java.io.File;
import java.util.logging.Level;

public class Main {

    public static void main(String[] args) throws Exception {

        System.setProperty("sun.awt.exception.handler", AwtExceptionHandler.class.getName());

        Log.get().setLevel(Level.FINE);

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
        Project project;
        String lastOpenedProject = Settings.get(Setting.LAST_OPENED_PROJECT);
        File romFile = null;
        if (lastOpenedProject != null) {
            File lastOpenedProjectFile = new File(lastOpenedProject);
            if (lastOpenedProjectFile.exists()) {
                splashForm.setProgress(0.4f, "Loading " + lastOpenedProjectFile.getName() + "...");

                try {
                    project = Project.loadFromArchive(lastOpenedProjectFile);
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
                    project = Project.builder().build();
                }
            } else {
                project = Project.builder().build();
                Log.ui().log(Level.WARNING, "Last opened project at " +
                        lastOpenedProjectFile.getPath() + " does not exist!");
            }
        } else {
            project = Project.builder().build();
            Log.ui().log(Level.INFO, "Opened a new project.");
        }


        splashForm.setProgress(0.5f, "Initializing UI...");
        Editor editorForm = new Editor(project);

        splashForm.setProgress(0.75f, "Opening ROM...");
        editorForm.openRom(romFile, project);

        splashForm.setProgress(1.0f, "Opening Atlas...");
        Thread.sleep(500L);
        splashForm.dispose();

        java.awt.EventQueue.invokeLater(() -> {
            editorForm.setVisible(true);
            Log.get().log(Level.FINE, "Application started.");
        });
    }

}
