package com.github.manevolent.atlas.ui.component.tab;

import com.github.manevolent.atlas.ui.Icons;
import com.github.manevolent.atlas.ui.EditorForm;
import org.kordamp.ikonli.carbonicons.CarbonIcons;

import javax.swing.*;

import static com.github.manevolent.atlas.ui.Fonts.getTextColor;

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
        return Icons.get(CarbonIcons.PRODUCT, getTextColor());
    }

    @Override
    protected void initComponent(JPanel panel) {

    }

    public void update() {

    }
}
