package com.github.manevolent.atlas.definition;

import java.io.IOException;

public interface FlashSource {

    int read(byte[] dst, int flashOffs, int offs, int len) throws IOException;

    int read(int flashOffs) throws IOException;

    void write(byte[] bytes, int flashOffs, int offs, int len) throws IOException;

    default void write(byte[] bytes, int flashOffs, int offs) throws IOException {
        write(bytes, flashOffs, offs, bytes.length);
    }

}
