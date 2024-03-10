package com.github.manevolent.atlas.ui.component.menu.table;

import com.github.manevolent.atlas.model.Table;
import com.github.manevolent.atlas.ui.component.window.TableEditor;

import javax.swing.*;

import static javax.swing.JOptionPane.QUESTION_MESSAGE;

public class FileMenu extends TableEditorMenu {
    public FileMenu(TableEditor editor) {
        super(editor);
    }

    @Override
    protected void initComponent(JMenu menu) {
        menu.setText("File");

        JMenuItem loadTable = new JMenuItem("Import Table...");
        loadTable.addActionListener(e -> {
        });
        menu.add(loadTable);

        JMenuItem saveTable = new JMenuItem("Export Table...");
        loadTable.addActionListener(e -> {
        });
        menu.add(saveTable);

        JMenuItem closeTable = new JMenuItem("Close Table");
        closeTable.addActionListener(e -> {
            getParent().getComponent().doDefaultCloseAction();
        });
        menu.add(closeTable);

        menu.addSeparator();

        JMenuItem editTable = new JMenuItem("Edit Table Definition");
        editTable.addActionListener(e -> {
            getParent().getParent().openTableDefinition(getParent().getTable());
        });
        menu.add(editTable);

        JMenuItem copyTable = new JMenuItem("Copy Table Definition...");
        copyTable.addActionListener(e -> {
            Table table =  getParent().getTable();
            String newTableName = (String) JOptionPane.showInputDialog(getParent().getParent(),
                    "Specify a name", "New Table",
                    QUESTION_MESSAGE, null, null, table.getName() + " (Copy)");

            if (newTableName == null || newTableName.isBlank()) {
                return;
            }

            Table newTable = table.copy();
            newTable.setName(newTableName);
            getParent().getParent().openTableDefinition(newTable);
        });
        menu.add(copyTable);
    }
}
