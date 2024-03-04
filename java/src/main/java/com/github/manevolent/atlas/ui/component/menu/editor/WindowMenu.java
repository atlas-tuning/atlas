package com.github.manevolent.atlas.ui.component.menu.editor;

import com.github.manevolent.atlas.ui.Icons;
import com.github.manevolent.atlas.ui.component.window.Window;
import com.github.manevolent.atlas.ui.window.EditorForm;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

public class WindowMenu extends EditorMenu {
    public WindowMenu(EditorForm editor) {
        super(editor);
    }

    @Override
    protected void initComponent(JMenu component) {
        super.initComponent(component);
        component.setText("Window");
        update(component);
    }

    public void update() {
        update(getComponent());
    }

    private void update(JMenu component) {
        // Clear all menu items
        component.removeAll();

        Collection<Window> openWindows = getParent().getOpenWindows();

        if (openWindows.size() <= 0) {
            JMenuItem menuItem = new JMenuItem("No active windows");
            menuItem.setEnabled(false);
            component.add(menuItem);
        }

        for (Window openWindow : openWindows) {
            Icon icon;

            if (openWindow.getComponent().isSelected()) {
                icon = Icons.get(CarbonIcons.CHECKMARK_OUTLINE, Color.WHITE);
            } else {
                icon = openWindow.getIcon();
            }

            JMenuItem menuItem = new JMenuItem(openWindow.getComponent().getTitle(), icon);
            menuItem.setSelected(openWindow.getComponent().isSelected());
            menuItem.addActionListener(e -> openWindow.focus());
            component.add(menuItem);
        }
    }
}
