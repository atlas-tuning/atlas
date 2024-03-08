package com.github.manevolent.atlas.ui.component;

import com.github.manevolent.atlas.ui.component.window.TableEditor;

import java.awt.*;

public abstract class TableEditorComponent<T extends Component> extends AtlasComponent<T, TableEditor> {
    protected TableEditorComponent(TableEditor editor) {
        super(editor);
    }
}
