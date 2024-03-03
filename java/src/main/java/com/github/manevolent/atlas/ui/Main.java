package com.github.manevolent.atlas.ui;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.github.manevolent.atlas.definition.Rom;
import com.github.manevolent.atlas.definition.builtin.SubaruWRX2022MT;
import com.github.manevolent.atlas.ui.window.EditorForm;
import com.github.manevolent.atlas.ui.window.SplashForm;

public class Main {

    public static void main(String[] args) throws Exception {
        FlatDarculaLaf.setup();

        SplashForm splashForm = new SplashForm();
        java.awt.EventQueue.invokeLater(() -> {
            splashForm.setVisible(true);
        });

        // TODO: Load resources?

        Thread.sleep(250L);
        splashForm.setProgress(0f);
        Rom rom = SubaruWRX2022MT.newRom();

        splashForm.setProgress(1.0f);
        Thread.sleep(500L);
        splashForm.dispose();

        EditorForm editorForm = new EditorForm(rom);
        editorForm.openRom(rom);

        java.awt.EventQueue.invokeLater(() -> {
            editorForm.setVisible(true);
        });
    }

}
