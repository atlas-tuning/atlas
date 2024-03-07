package com.github.manevolent.atlas.model;

import java.io.IOException;

public interface MemorySource {

    int read(byte[] dst, long flashOffs, int offs, int len) throws IOException;

    int read(long flashOffs) throws IOException;

    void write(byte[] bytes, long flashOffs, int offs, int len) throws IOException;

    default void write(byte[] bytes, long flashOffs, int offs) throws IOException {
        write(bytes, flashOffs, offs, bytes.length);
    }

    default boolean isLocal() {
        return true;
    }

}
