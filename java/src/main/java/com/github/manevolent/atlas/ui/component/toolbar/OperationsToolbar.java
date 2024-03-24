package com.github.manevolent.atlas.ui.component.toolbar;

import com.github.manevolent.atlas.ui.component.tab.FormatsTab;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import javax.swing.*;
import java.awt.*;

public class OperationsToolbar extends Toolbar<FormatsTab> {
    public OperationsToolbar(FormatsTab editor) {
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
        toolbar.add(makeSmallButton(FontAwesomeSolid.CARET_UP, "up", "Move operation up", _ -> {
            getParent().moveUp();
        }));
        toolbar.add(makeSmallButton(FontAwesomeSolid.CARET_DOWN, "down", "Move operation down", _ -> {
            getParent().moveDown();
        }));
        toolbar.addSeparator();
        toolbar.add(makeSmallButton(FontAwesomeSolid.EDIT, "edit", "Edit operation", _ -> {
            getParent().editOperation();
        }));
        toolbar.add(makeSmallButton(FontAwesomeSolid.PLUS, "new", "Add operation", _ -> {
            getParent().addOperation();
        }));
        toolbar.add(makeSmallButton(FontAwesomeSolid.TRASH, "delete", "Delete operation", _ -> {
            getParent().deleteOperation();
        }));
        toolbar.addSeparator();
        toolbar.add(makeSmallButton(FontAwesomeSolid.VIAL, "test", "Test operation", _ -> {
            getParent().testOperation();
        }));
    }
}
