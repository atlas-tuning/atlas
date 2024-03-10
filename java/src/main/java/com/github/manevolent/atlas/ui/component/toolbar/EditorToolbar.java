package com.github.manevolent.atlas.ui.component.toolbar;

import com.github.manevolent.atlas.ui.Editor;
import com.github.manevolent.atlas.ui.behavior.EditHistory;
import com.github.manevolent.atlas.ui.behavior.WindowHistory;
import com.github.manevolent.atlas.ui.component.window.Window;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import javax.swing.*;
import java.util.List;

public class EditorToolbar extends Toolbar<Editor> {
    private JButton left, right;
    private JButton undo, redo;
    public EditorToolbar(Editor editor) {
        super(editor);
    }

    @Override
    protected void initComponent(JToolBar toolbar) {
        toolbar.add(makeSmallButton(FontAwesomeSolid.FOLDER_OPEN, "open", "Open project...", _ -> {
            getParent().openRom();
        }));
        toolbar.add(makeSmallButton(FontAwesomeSolid.SAVE, "save", "Save project", _ -> {
            getParent().saveRom();
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
