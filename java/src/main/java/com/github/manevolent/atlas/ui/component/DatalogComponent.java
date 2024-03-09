package com.github.manevolent.atlas.ui.component;

import com.github.manevolent.atlas.ui.component.window.DatalogWindow;

import java.awt.*;

public abstract class DatalogComponent<T extends Component> extends AtlasComponent<T, DatalogWindow>  {
    protected DatalogComponent(DatalogWindow editor) {
        super(editor);
    }

}
