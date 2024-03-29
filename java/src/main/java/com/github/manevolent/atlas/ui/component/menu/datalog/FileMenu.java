package com.github.manevolent.atlas.ui.component.menu.datalog;

import com.github.manevolent.atlas.ui.component.window.DatalogWindow;

import javax.swing.*;

public class FileMenu extends DatalogMenu {
    public FileMenu(DatalogWindow editor) {
        super(editor);
    }

    @Override
    protected void initComponent(JMenu fileMenu) {
        fileMenu.setText("File");

        JMenuItem saveDatalog = new JMenuItem("Export Datalog...");
        saveDatalog.addActionListener(e -> {
            getParent().saveDatalog(true);
        });
        fileMenu.add(saveDatalog);

        JMenuItem saveVisible = new JMenuItem("Export Visible...");
        saveVisible.addActionListener(e -> {
            getParent().saveDatalog(false);
        });
        fileMenu.add(saveVisible);
        fileMenu.addSeparator();

        JMenuItem openDatalog = new JMenuItem("Open Datalog...");
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
