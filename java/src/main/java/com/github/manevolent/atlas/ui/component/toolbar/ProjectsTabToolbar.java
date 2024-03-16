package com.github.manevolent.atlas.ui.component.toolbar;

import com.github.manevolent.atlas.ui.component.tab.ProjectTab;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import javax.swing.*;
import java.awt.*;

public class ProjectsTabToolbar extends Toolbar<ProjectTab> {
    public ProjectsTabToolbar(ProjectTab editor) {
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
        toolbar.add(makeSmallButton(FontAwesomeSolid.PLUS, "new", "New setting", _ -> {
            getParent().newSetting();
        }));

        toolbar.add(makeSmallButton(FontAwesomeSolid.EDIT, "edit", "Edit setting", _ -> {
            getParent().editSetting();
        }));

        toolbar.add(makeSmallButton(FontAwesomeSolid.COPY, "copy", "Copy setting", _ -> {
            getParent().copySetting();
        }));

        toolbar.add(makeSmallButton(FontAwesomeSolid.TRASH, "delete", "Delete setting", _ -> {
            getParent().deleteSetting();
        }));
    }
}
