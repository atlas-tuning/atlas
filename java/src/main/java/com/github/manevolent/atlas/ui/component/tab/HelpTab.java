package com.github.manevolent.atlas.ui.component.tab;

import com.github.manevolent.atlas.ui.IconHelper;
import com.github.manevolent.atlas.ui.window.EditorForm;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import java.awt.*;

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
        return IconHelper.get(CarbonIcons.HELP, Color.WHITE);
    }

    @Override
    protected void initComponent(JPanel panel) {

    }
}
