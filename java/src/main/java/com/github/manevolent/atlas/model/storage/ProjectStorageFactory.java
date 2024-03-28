package com.github.manevolent.atlas.model.storage;

import com.github.manevolent.atlas.ui.behavior.ProgressListener;

public interface ProjectStorageFactory {

    ProjectStorage createStorage(ProgressListener progressListener);

    default ProjectStorage createStorage() {
        return createStorage((mesage, progress) -> { /* do nothing */ });
    }

}
