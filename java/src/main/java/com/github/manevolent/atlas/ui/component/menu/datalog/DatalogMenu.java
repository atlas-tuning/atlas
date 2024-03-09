package com.github.manevolent.atlas.ui.component.menu.datalog;

import com.github.manevolent.atlas.ui.component.DatalogComponent;
import com.github.manevolent.atlas.ui.component.window.DatalogWindow;

import javax.swing.*;

public abstract class DatalogMenu extends DatalogComponent<JMenu> {
    protected DatalogMenu(DatalogWindow window) {
        super(window);
    }

    @Override
    protected JMenu newComponent() {
        return new JMenu();
    }
}
