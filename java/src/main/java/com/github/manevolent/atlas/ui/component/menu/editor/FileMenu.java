package com.github.manevolent.atlas.ui.component.menu.editor;

import com.github.manevolent.atlas.ui.window.EditorForm;
import com.github.manevolent.atlas.ui.window.NewRomForm;

import javax.swing.*;

public class FileMenu extends EditorMenu {
    public FileMenu(EditorForm editor) {
        super(editor);
    }

    @Override
    protected void initComponent(JMenu fileMenu) {
        fileMenu.setText("File");

        JMenuItem newRom = new JMenuItem("New ROM...");
        newRom.addActionListener(e -> {
            NewRomForm newRomForm = new NewRomForm();
            newRomForm.setVisible(true);
        });
        fileMenu.add(newRom);

        JMenuItem openRom = new JMenuItem("Open ROM...");
        fileMenu.add(openRom);

        JMenuItem recentRoms = new JMenu("Recent ROMs");

        //TODO: Recent roms
        JMenuItem noRecentRoms = new JMenuItem("No recent ROMs");
        noRecentRoms.setEnabled(false);
        recentRoms.add(noRecentRoms);

        fileMenu.add(recentRoms);

        fileMenu.addSeparator();

        JMenuItem saveRom = new JMenuItem("Save ROM");
        saveRom.setEnabled(false);
        fileMenu.add(saveRom);

        fileMenu.addSeparator();
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener((e) -> getParent().exit());
        fileMenu.add(exit);
    }
}
