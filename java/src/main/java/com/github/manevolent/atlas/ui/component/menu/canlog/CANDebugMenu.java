package com.github.manevolent.atlas.ui.component.menu.canlog;

import com.github.manevolent.atlas.ui.component.CANLogComponent;
import com.github.manevolent.atlas.ui.component.window.CANDebugWindow;

import javax.swing.*;

public abstract class CANDebugMenu extends CANLogComponent<JMenu> {
    protected CANDebugMenu(CANDebugWindow window) {
        super(window);
    }

    @Override
    protected JMenu newComponent() {
        return new JMenu();
    }
}
