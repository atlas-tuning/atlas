package com.github.manevolent.atlas.ui.component.toolbar;

import com.github.manevolent.atlas.ui.component.window.DatalogWindow;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import javax.swing.*;
import java.awt.*;

public class DataLoggingToolbar extends Toolbar<DatalogWindow> {
    public DataLoggingToolbar(DatalogWindow editor) {
        super(editor);
    }

    @Override
    protected void preInitComponent(JToolBar toolbar) {
        super.preInitComponent(toolbar);

        toolbar.setOrientation(JToolBar.HORIZONTAL);
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY.darker()));
    }

    @Override
    protected void initComponent(JToolBar toolbar) {
        toolbar.addSeparator();
        toolbar.add(makeButton(FontAwesomeSolid.PAUSE, "pause", "Pause datalog"));
        toolbar.addSeparator();

    }
}
