package com.github.manevolent.atlas.ui.component.toolbar;

import com.github.manevolent.atlas.ui.component.tab.ConsoleTab;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import javax.swing.*;
import java.awt.*;

public class ConsoleTabToolbar extends Toolbar<ConsoleTab> {
    public ConsoleTabToolbar(ConsoleTab editor) {
        super(editor);
    }

    @Override
    protected void preInitComponent(JToolBar toolbar) {
        super.preInitComponent(toolbar);

        toolbar.setOrientation(JToolBar.VERTICAL);
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY.darker()));
    }

    @Override
    protected void initComponent(JToolBar toolbar) {
        toolbar.add(makeSmallButton(FontAwesomeSolid.COPY, "copy", "Copy contents", _ -> {
            getParent().copyConsole();
        }));
        toolbar.add(makeSmallButton(FontAwesomeSolid.SAVE, "save", "Save...", _ -> {
            getParent().saveConsole();
        }));
        //TODO
        //toolbar.add(makeSmallButton(FontAwesomeSolid.SORT_AMOUNT_DOWN, "scroll", "Auto-scroll"));
        toolbar.addSeparator();
        toolbar.add(makeSmallButton(FontAwesomeSolid.TRASH, "clear", "Clear all", _ -> {
            getParent().clearConsole();
        }));
    }
}
