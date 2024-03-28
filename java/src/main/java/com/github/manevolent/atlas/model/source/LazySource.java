package com.github.manevolent.atlas.model.source;

import com.github.manevolent.atlas.model.MemorySection;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class LazySource implements com.github.manevolent.atlas.model.MemorySource {
    private final Loader loader;
    private ArraySource backing;
    private boolean dirty;

    public LazySource(Loader loader) {
        this.loader = loader;
    }

    public ArraySource load() throws IOException {
        if (backing == null) {
            backing = loader.load();
        }

        return backing;
    }

    @Override
    public long getBaseAddress() {
        try {
            return load().getBaseAddress();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getLength() {
        try {
            return load().getLength();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int read(byte[] dst, long memoryBase, int offs, int len) throws IOException {
        return load().read(dst, memoryBase, offs, len);
    }

    @Override
    public int read(long position) throws IOException {
        return load().read(position);
    }

    @Override
    public void write(byte[] bytes, long memoryBase, int offs, int len) throws IOException {
        load().write(bytes, memoryBase, offs, len);

        if (len > 0) {
            setDirty(true);
        }
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean isDirty() {
        return dirty;
    }

    public static LazySource fromZipEntry(File zipFile, FileHeader calibrationFile, MemorySection section) {
        return new LazySource(new ZipLoader(zipFile, calibrationFile, section));
    }

    public interface Loader {
        ArraySource load() throws IOException;
    }

    public static class ZipLoader implements Loader {
        private final File zipFile;
        private final FileHeader calibrationFile;
        private final MemorySection section;

        public ZipLoader(File zipFile, FileHeader calibrationFile, MemorySection section) {
            this.zipFile = zipFile;
            this.calibrationFile = calibrationFile;
            this.section = section;
        }

        @Override
        public ArraySource load() throws IOException {
            try (ZipFile zip = new ZipFile(zipFile)) {
                FileHeader existing = zip.getFileHeader(calibrationFile.getFileName());
                if (existing == null) {
                    throw new FileNotFoundException(calibrationFile.getFileName());
                }
                byte[] data = zip.getInputStream(existing).readAllBytes();
                return new ArraySource(section.getBaseAddress(), data, 0, data.length);
            }
        }
    }
}
