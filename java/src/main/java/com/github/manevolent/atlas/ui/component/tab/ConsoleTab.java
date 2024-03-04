package com.github.manevolent.atlas.ui.component.tab;

import com.github.manevolent.atlas.ui.Icons;
import com.github.manevolent.atlas.ui.window.EditorForm;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import java.awt.*;

public class ConsoleTab extends Tab {
    public ConsoleTab(EditorForm editor) {
        super(editor);
    }

    @Override
    public String getTitle() {
        return "Console";
    }

    @Override
    public Icon getIcon() {
        return Icons.get(CarbonIcons.TERMINAL, Color.WHITE);
    }

    @Override
    protected void initComponent(JPanel panel) {

    }
}
