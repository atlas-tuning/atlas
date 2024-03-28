package com.github.manevolent.atlas.model;

import java.io.IOException;

public interface MemorySource {

    long getBaseAddress();

    int getLength();

    int read(byte[] dst, long memoryBase, int offs, int len) throws IOException;

    int read(long position) throws IOException;

    void write(byte[] bytes, long memoryBase, int offs, int len) throws IOException;

    default byte[] readFully() throws IOException {
        byte[] data = new byte[getLength()];
        read(data, getBaseAddress(), 0, data.length);
        return data;
    }

    default boolean isLocal() {
        return true;
    }

}
