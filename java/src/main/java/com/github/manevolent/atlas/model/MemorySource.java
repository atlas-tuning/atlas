package com.github.manevolent.atlas.model;

import java.io.IOException;

public interface MemorySource {

    long getBaseAddress();

    int getLength();

    int read(byte[] dst, long memoryBase, int offs, int len) throws IOException;

    int read(long position) throws IOException;

    void write(byte[] bytes, long memoryBase, int offs, int len) throws IOException;

    default boolean isLocal() {
        return true;
    }

}
