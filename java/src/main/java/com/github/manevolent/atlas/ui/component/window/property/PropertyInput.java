package com.github.manevolent.atlas.ui.component.window.property;

import com.github.manevolent.atlas.model.ProjectProperty;
import com.github.manevolent.atlas.ui.Editor;

public interface PropertyInput {

    boolean update(Editor editor, ProjectProperty property);

    ProjectProperty newInstance();

    default ProjectProperty create(Editor editor) {
        ProjectProperty instance = newInstance();
        update(editor, instance);
        return instance;
    }

}
