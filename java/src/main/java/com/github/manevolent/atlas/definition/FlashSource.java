package com.github.manevolent.atlas.definition;

import java.io.IOException;

public interface FlashSource {

    int read(byte[] dst, int offs, int len) throws IOException;
    int read(int offs) throws IOException;

}
