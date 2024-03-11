package com.github.manevolent.atlas.ui.component.menu.datalog;

import com.github.manevolent.atlas.ui.util.Icons;
import com.github.manevolent.atlas.ui.component.window.DatalogPage;
import com.github.manevolent.atlas.ui.component.window.DatalogWindow;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;

public class ViewMenu extends DatalogMenu {
    public ViewMenu(DatalogWindow editor) {
        super(editor);
    }

    @Override
    protected void initComponent(JMenu viewMenu) {
        viewMenu.setText("View");

        JMenuItem viewAll = new JMenuItem("Fit to width", Icons.get(CarbonIcons.FIT_TO_SCREEN));
        viewAll.addActionListener(e -> {
            DatalogPage activePage = getParent().getActivePage();
            if (activePage != null) {
                activePage.zoomIn();
            }
        });
        viewMenu.add(viewAll);

    }
}
