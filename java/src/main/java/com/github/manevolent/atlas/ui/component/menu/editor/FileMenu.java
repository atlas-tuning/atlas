package com.github.manevolent.atlas.ui.component.menu.editor;

import com.github.manevolent.atlas.ui.Editor;
import com.github.manevolent.atlas.ui.NewRomForm;
import com.github.manevolent.atlas.ui.util.Icons;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;

public class FileMenu extends EditorMenu {
    public FileMenu(Editor editor) {
        super(editor);
    }

    @Override
    protected void initComponent(JMenu fileMenu) {
        fileMenu.setText("File");

        JMenuItem newRom = new JMenuItem("New Project...");
        newRom.setIcon(Icons.get(CarbonIcons.DOCUMENT));
        newRom.addActionListener(e -> {
            NewRomForm newRomForm = new NewRomForm();
            newRomForm.setVisible(true);
        });
        fileMenu.add(newRom);

        JMenuItem openRom = new JMenuItem("Open Project...");
        openRom.setIcon(Icons.get(CarbonIcons.FOLDER));
        openRom.addActionListener(e -> {
            getParent().openProject();
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
        saveRom.setIcon(Icons.get(CarbonIcons.SAVE));
        saveRom.addActionListener((e) -> {
            getParent().saveProject();
        });
        fileMenu.add(saveRom);

        fileMenu.addSeparator();

        JMenuItem settings = new JMenuItem("Atlas Settings...");
        settings.setIcon(Icons.get(CarbonIcons.SETTINGS));
        settings.addActionListener(e -> {

        });
        fileMenu.add(settings);

        JMenuItem projectSettings = new JMenuItem("Project Settings...");
        projectSettings.setIcon(Icons.get(CarbonIcons.PRODUCT));
        projectSettings.addActionListener(e -> {
            getParent().openProjectSettings();
        });
        fileMenu.add(projectSettings);

        JMenuItem openDeviceSettings = new JMenuItem("Device Settings...");
        openDeviceSettings.setIcon(Icons.get(CarbonIcons.TOOL_BOX));
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
