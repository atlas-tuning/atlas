package com.github.manevolent.atlas.ui.component.toolbar;

import com.github.manevolent.atlas.ui.component.tab.FormatsTab;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import javax.swing.*;
import java.awt.*;

public class FormatsTabToolbar extends Toolbar<FormatsTab> {
    public FormatsTabToolbar(FormatsTab editor) {
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
        toolbar.add(makeSmallButton(FontAwesomeSolid.PLUS, "new", "New format", _ -> {
            getParent().newFormat();
        }));

        toolbar.add(makeSmallButton(FontAwesomeSolid.TRASH, "delete", "Delete format", _ -> {
            getParent().deleteFormat();
        }));

        toolbar.add(makeSmallButton(FontAwesomeSolid.COPY, "copy", "Copy format", _ -> {
            getParent().copyFormat();
        }));
    }
}
