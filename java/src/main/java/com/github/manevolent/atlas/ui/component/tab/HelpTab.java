package com.github.manevolent.atlas.ui.component.tab;

import com.github.manevolent.atlas.ui.util.Icons;
import com.github.manevolent.atlas.ui.Editor;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;

import static com.github.manevolent.atlas.ui.util.Fonts.getTextColor;

public class HelpTab extends Tab {
    public HelpTab(Editor editor, JTabbedPane tabbedPane) {
        super(editor, tabbedPane);
    }

    @Override
    public String getTitle() {
        return "Help";
    }

    @Override
    public Icon getIcon() {
        return Icons.get(CarbonIcons.HELP, getTextColor());
    }

    @Override
    protected void initComponent(JPanel panel) {

    }
}
