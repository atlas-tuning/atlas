package com.github.manevolent.atlas.model.source;

import com.github.manevolent.atlas.model.MemorySource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileSource implements MemorySource {
    private final RandomAccessFile randomAccessFile;

    public FileSource(File flashFile) throws FileNotFoundException {
        this.randomAccessFile = new RandomAccessFile(flashFile, "r");
    }

    @Override
    public int read(byte[] dst, long flashOffs, int offs, int len) throws IOException {
        randomAccessFile.seek(flashOffs);
        return randomAccessFile.read(dst, offs, len);
    }

    @Override
    public int read(long offs) throws IOException {
        randomAccessFile.seek(offs);
        return randomAccessFile.read();
    }

    @Override
    public void write(byte[] bytes, long flashOffs, int offs, int len) throws IOException {
        randomAccessFile.seek(flashOffs);
        randomAccessFile.write(bytes, offs, len);
    }
}
