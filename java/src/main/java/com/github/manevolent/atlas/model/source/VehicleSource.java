package com.github.manevolent.atlas.model.source;

import com.github.manevolent.atlas.model.MemorySource;

import java.io.IOException;

//TODO
public class VehicleSource implements MemorySource {
    @Override
    public int read(byte[] dst, long flashOffs, int offs, int len) throws IOException {
        return 0;
    }

    @Override
    public int read(long position) throws IOException {
        return 0;
    }

    @Override
    public void write(byte[] bytes, long flashOffs, int offs, int len) throws IOException {

    }

    @Override
    public boolean isLocal() {
        return false;
    }
}
