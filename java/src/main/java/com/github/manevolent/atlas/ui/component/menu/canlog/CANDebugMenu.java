package com.github.manevolent.atlas.ui.component.menu.canlog;

import com.github.manevolent.atlas.ui.component.CANDebugComponent;
import com.github.manevolent.atlas.ui.component.window.CANDebugWindow;

import javax.swing.*;

public abstract class CANDebugMenu extends CANDebugComponent<JMenu> {
    protected CANDebugMenu(CANDebugWindow window) {
        super(window);
    }

    @Override
    protected JMenu newComponent() {
        return new JMenu();
    }
}
