package com.github.manevolent.atlas.ui.behavior;

import com.github.manevolent.atlas.ui.Editor;
import com.github.manevolent.atlas.ui.component.window.Window;

public class WindowAction implements Action {
    private final Editor editor;
    private final Window priorWindow, newWindow;

    public WindowAction(Editor editor, Window priorWindow, Window newWindow) {
        this.editor = editor;
        this.priorWindow = priorWindow;
        this.newWindow = newWindow;
    }

    @Override
    public boolean undo() {
        if (priorWindow != null && editor.hasWindow(priorWindow)) {
            priorWindow.focus();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean redo() {
        if (newWindow != null && editor.hasWindow(newWindow)) {
            newWindow.focus();
            return true;
        } else {
            return false;
        }
    }
}
