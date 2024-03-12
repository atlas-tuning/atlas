package com.github.manevolent.atlas.ui.component.menu.editor;

import com.github.manevolent.atlas.ui.Editor;
import com.github.manevolent.atlas.ui.NewRomForm;

import javax.swing.*;

public class FileMenu extends EditorMenu {
    public FileMenu(Editor editor) {
        super(editor);
    }

    @Override
    protected void initComponent(JMenu fileMenu) {
        fileMenu.setText("File");

        JMenuItem newRom = new JMenuItem("New Project...");
        newRom.addActionListener(e -> {
            NewRomForm newRomForm = new NewRomForm();
            newRomForm.setVisible(true);
        });
        fileMenu.add(newRom);

        JMenuItem openRom = new JMenuItem("Open Project...");
        openRom.addActionListener(e -> {
            getParent().openRom();
        });
        fileMenu.add(openRom);

        JMenuItem recentRoms = new JMenu("Recent Projects");

        //TODO: Recent roms
        JMenuItem noRecentRoms = new JMenuItem("No recent projects");
        noRecentRoms.setEnabled(false);
        recentRoms.add(noRecentRoms);

        fileMenu.add(recentRoms);

        fileMenu.addSeparator();

        JMenuItem saveRom = new JMenuItem("Save Project");
        saveRom.addActionListener((e) -> {
            getParent().saveRom();
        });
        fileMenu.add(saveRom);

        fileMenu.addSeparator();

        JMenuItem openDeviceSettings = new JMenuItem("Device Settings...");
        openDeviceSettings.addActionListener((e) -> {
            getParent().openDeviceSettings();
        });
        fileMenu.add(openDeviceSettings);

        fileMenu.addSeparator();
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener((e) -> getParent().exit());
        fileMenu.add(exit);
    }
}
