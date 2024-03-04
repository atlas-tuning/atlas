package com.github.manevolent.atlas.ui.component;

import com.github.manevolent.atlas.ui.window.EditorForm;

import java.awt.*;

public abstract class EditorComponent<T extends Component> extends AtlasComponent<T, EditorForm> {
    protected EditorComponent(EditorForm editor) {
        super(editor);
    }
}
