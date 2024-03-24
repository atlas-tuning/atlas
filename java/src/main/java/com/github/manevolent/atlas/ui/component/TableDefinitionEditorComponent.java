package com.github.manevolent.atlas.ui.component;

import com.github.manevolent.atlas.ui.component.window.TableDefinitionEditor;
import com.github.manevolent.atlas.ui.component.window.TableEditor;

import java.awt.*;

public abstract class TableDefinitionEditorComponent<T extends Component> extends AtlasComponent<T, TableDefinitionEditor> {
    protected TableDefinitionEditorComponent(TableDefinitionEditor editor) {
        super(editor);
    }
}
