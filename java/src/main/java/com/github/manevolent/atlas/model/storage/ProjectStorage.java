package com.github.manevolent.atlas.model.storage;

import com.github.manevolent.atlas.model.Project;

import java.io.File;

import java.io.IOException;

public interface ProjectStorage {
    Project load(File file) throws IOException;
    void save(Project project, File file) throws IOException;
}
