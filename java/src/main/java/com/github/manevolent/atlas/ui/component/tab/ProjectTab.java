package com.github.manevolent.atlas.ui.component.tab;

import com.github.manevolent.atlas.ui.IconHelper;
import com.github.manevolent.atlas.ui.window.EditorForm;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;
import java.awt.*;

public class ProjectTab extends Tab {
    public ProjectTab(EditorForm editor) {
        super(editor);
    }

    @Override
    public String getTitle() {
        return "Project";
    }

    @Override
    public Icon getIcon() {
        return IconHelper.get(CarbonIcons.PRODUCT, Color.WHITE);
    }

    @Override
    protected void initComponent(JPanel panel) {

    }
}
