package com.github.manevolent.atlas.ui;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.github.manevolent.atlas.model.Rom;
import com.github.manevolent.atlas.model.builtin.SubaruWRX2022MT;
import com.github.manevolent.atlas.logging.Log;
import com.github.manevolent.atlas.ui.window.EditorForm;
import com.github.manevolent.atlas.ui.window.SplashForm;

import java.util.logging.Level;

public class Main {

    public static void main(String[] args) throws Exception {
        System.setProperty("sun.awt.exception.handler", AwtExceptionHandler.class.getName());

        SplashForm splashForm = new SplashForm();
        java.awt.EventQueue.invokeLater(() -> {
            splashForm.setVisible(true);
        });

        // TODO: Load resources?

        Thread.sleep(250L);
        splashForm.setProgress(0f);

        FlatDarculaLaf.setup();
        splashForm.setProgress(0.1f);

        Rom rom = SubaruWRX2022MT.newRom();
        splashForm.setProgress(0.5f);

        EditorForm editorForm = new EditorForm(rom);
        splashForm.setProgress(0.7f);

        editorForm.openRom(rom);
        splashForm.setProgress(1.0f);

        Thread.sleep(500L);
        splashForm.dispose();

        java.awt.EventQueue.invokeLater(() -> {
            editorForm.setVisible(true);
            Log.get().log(Level.FINE, "Application started.");
        });
    }

}
