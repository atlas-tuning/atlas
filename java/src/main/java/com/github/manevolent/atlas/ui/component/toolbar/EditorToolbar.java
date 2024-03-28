package com.github.manevolent.atlas.ui.component.toolbar;

import com.github.manevolent.atlas.ui.Editor;
import com.github.manevolent.atlas.ui.behavior.EditHistory;
import com.github.manevolent.atlas.ui.behavior.WindowHistory;

import org.kordamp.ikonli.carbonicons.CarbonIcons;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import javax.swing.*;

public class EditorToolbar extends Toolbar<Editor> {
    private JButton left, right;
    private JButton undo, redo;
    public EditorToolbar(Editor editor) {
        super(editor);
    }

    @Override
    protected void initComponent(JToolBar toolbar) {
        toolbar.add(makeSmallButton(FontAwesomeSolid.FOLDER_OPEN, "open", "Open project...", _ -> {
            getParent().openProject();
        }));
        toolbar.add(makeSmallButton(FontAwesomeSolid.SAVE, "save", "Save project", _ -> {
            getParent().saveProject();
        }));

        toolbar.addSeparator();

        toolbar.add(left = makeSmallButton(FontAwesomeSolid.ARROW_LEFT, "left", "Last location", _ -> {
            getParent().getWindowHistory().undo();
            update();
        }));

        toolbar.add(right = makeSmallButton(FontAwesomeSolid.ARROW_RIGHT, "right", "Next location", _ -> {
            getParent().getWindowHistory().redo();
            update();
        }));

        toolbar.addSeparator();

        toolbar.add(undo = makeSmallButton(FontAwesomeSolid.UNDO, "undo", "Undo", _ -> {
            getParent().getEditHistory().undo();
            update();
        }));

        toolbar.add(redo = makeSmallButton(FontAwesomeSolid.REDO, "redo", "Redo", _ -> {
            getParent().getEditHistory().redo();
            update();
        }));

        toolbar.addSeparator();

        toolbar.add(makeButton(CarbonIcons.DATA_TABLE_REFERENCE, "newtable", "New Table...", _ -> {
            getParent().newTable();
        }));

        toolbar.add(makeButton(CarbonIcons.CHART_AVERAGE, "datalogging", "Open Data Logging", _ -> {
            getParent().openDataLogging();
        }));

        toolbar.add(makeButton(CarbonIcons.DEBUG, "canlogging", "Open CAN Debugging", _ -> {
            getParent().openCanLogging();
        }));

        update();
    }

    public void update() {
        EditHistory editHistory = getParent().getEditHistory();
        undo.setEnabled(editHistory != null && editHistory.canUndo());
        redo.setEnabled(editHistory != null && editHistory.canRedo());

        WindowHistory windowHistory = getParent().getWindowHistory();
        left.setEnabled(windowHistory != null && windowHistory.canUndo());
        right.setEnabled(windowHistory != null && windowHistory.canRedo());
    }
}
