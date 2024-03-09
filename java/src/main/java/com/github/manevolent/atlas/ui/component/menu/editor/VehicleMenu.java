package com.github.manevolent.atlas.ui.component.menu.editor;

import com.github.manevolent.atlas.ui.EditorForm;
import com.github.manevolent.atlas.ui.Menus;
import com.github.manevolent.atlas.ui.NewRomForm;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;

public class VehicleMenu extends EditorMenu {
    public VehicleMenu(EditorForm editor) {
        super(editor);
    }

    @Override
    protected void initComponent(JMenu menu) {
        menu.setText("Vehicle");

        JMenuItem connect = new JMenuItem("Connect...");
        connect.addActionListener(e -> {
            NewRomForm newRomForm = new NewRomForm();
            newRomForm.setVisible(true);
        });
        menu.add(connect);

        JMenuItem disconnect = new JMenuItem("Disconnect");
        disconnect.addActionListener(e -> {

        });
        menu.add(disconnect);

        menu.addSeparator();

        JMenuItem menuItem = Menus.item(CarbonIcons.CHART_AVERAGE, "Data Logging", (e) -> {
            getParent().openDataLogging();
        });
        menu.add(menuItem);
    }
}
