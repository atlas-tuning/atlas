package com.github.manevolent.atlas.ui.component;

import com.github.manevolent.atlas.ui.component.window.CANDebugWindow;

import java.awt.*;

public abstract class CANLogComponent<T extends Component> extends AtlasComponent<T, CANDebugWindow>  {
    protected CANLogComponent(CANDebugWindow editor) {
        super(editor);
    }

}
