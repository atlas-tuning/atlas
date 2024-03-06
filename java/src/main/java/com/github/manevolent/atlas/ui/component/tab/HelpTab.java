package com.github.manevolent.atlas.ui.component.tab;

import com.github.manevolent.atlas.ui.Icons;
import com.github.manevolent.atlas.ui.window.EditorForm;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import java.awt.*;

import static com.github.manevolent.atlas.ui.Fonts.getTextColor;

public class HelpTab extends Tab {
    public HelpTab(EditorForm editor) {
        super(editor);
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
