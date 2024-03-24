package com.github.manevolent.atlas.ui.component;

import com.github.manevolent.atlas.ui.component.window.CANDebugWindow;

import java.awt.*;

public abstract class CANDebugComponent<T extends Component> extends AtlasComponent<T, CANDebugWindow>  {
    protected CANDebugComponent(CANDebugWindow editor) {
        super(editor);
    }

}
