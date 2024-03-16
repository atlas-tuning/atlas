package com.github.manevolent.atlas.ui.component.toolbar;

import com.github.manevolent.atlas.ui.component.tab.FormatsTab;
import com.github.manevolent.atlas.ui.component.tab.ParametersTab;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import javax.swing.*;
import java.awt.*;

public class ParametersTabToolbar extends Toolbar<ParametersTab> {
    public ParametersTabToolbar(ParametersTab editor) {
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
        toolbar.add(makeSmallButton(FontAwesomeSolid.PLUS, "new", "New parameter", _ -> {
            getParent().newParameter();
        }));

        toolbar.add(makeSmallButton(FontAwesomeSolid.TRASH, "delete", "Delete parameter", _ -> {
            getParent().deleteParameter();
        }));

        toolbar.add(makeSmallButton(FontAwesomeSolid.COPY, "copy", "Copy parameter", _ -> {
            getParent().copyParameter();
        }));
    }
}
