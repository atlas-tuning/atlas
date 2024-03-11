package com.github.manevolent.atlas.ui.component.menu.canlog;

import com.github.manevolent.atlas.ui.component.window.CANDebugWindow;

import javax.swing.*;

public class FileMenu extends CANDebugMenu {
    public FileMenu(CANDebugWindow editor) {
        super(editor);
    }

    @Override
    protected void initComponent(JMenu fileMenu) {
        fileMenu.setText("File");

        JMenuItem saveDatalog = new JMenuItem("Export to File...");
        saveDatalog.addActionListener(e -> {
            getParent().saveSession();
        });
        fileMenu.add(saveDatalog);

        fileMenu.addSeparator();

        JMenuItem openDatalog = new JMenuItem("Open Session...");
        openDatalog.addActionListener(e -> {
        });
        fileMenu.add(openDatalog);

        fileMenu.addSeparator();

        //TODO
        JMenuItem close = new JMenuItem("Close");
        close.addActionListener((e) -> {

        });
        //fileMenu.add(close);

        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener((e) -> getParent().getComponent().doDefaultCloseAction());
        fileMenu.add(exit);
    }
}
