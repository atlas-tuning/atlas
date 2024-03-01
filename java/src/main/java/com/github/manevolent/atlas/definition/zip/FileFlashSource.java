package com.github.manevolent.atlas.definition.zip;

import com.github.manevolent.atlas.definition.FlashSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileFlashSource implements FlashSource {
    private final RandomAccessFile randomAccessFile;

    public FileFlashSource(File flashFile) throws FileNotFoundException {
        this.randomAccessFile = new RandomAccessFile(flashFile, "r");
    }

    @Override
    public int read(byte[] dst, int offs, int len) throws IOException {
        randomAccessFile.seek(offs);
        return randomAccessFile.read(dst, 0, len);
    }

    @Override
    public int read(int offs) throws IOException {
        randomAccessFile.seek(offs);
        return randomAccessFile.read();
    }
}
