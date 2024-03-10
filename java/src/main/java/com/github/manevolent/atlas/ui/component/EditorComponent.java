package com.github.manevolent.atlas.ui.component;

import com.github.manevolent.atlas.ui.Editor;

import java.awt.*;

public abstract class EditorComponent<T extends Component> extends AtlasComponent<T, Editor> {
    protected EditorComponent(Editor editor) {
        super(editor);
    }
}
