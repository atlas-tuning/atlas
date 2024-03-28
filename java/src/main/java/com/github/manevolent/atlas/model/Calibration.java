package com.github.manevolent.atlas.model;


import com.github.manevolent.atlas.model.source.ArraySource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class Calibration implements MemorySource {
    private UUID uuid;
    private String name;
    private boolean readonly;
    private MemorySource source;
    private MemorySection section;

    public Calibration() {
        this.uuid = UUID.randomUUID();
    }

    public Calibration(String name) {
        this();

        this.name = name;
    }

    public void updateSource(byte[] data) {
        updateSource(data, data.length);
    }

    public void updateSource(byte[] data, int length) {
        updateSource(data, 0, length);
    }

    public void updateSource(byte[] data, int offset, int length) {
        this.source = new ArraySource(section.getBaseAddress(), data, offset, length);
    }

    public boolean hasData() {
        return source != null;
    }

    public void setSource(MemorySource source) {
        this.source = source;
    }

    public int copyTo(OutputStream outputStream) throws IOException {
        int length = source.getLength();
        long base = source.getBaseAddress();
        for (int i = 0; i < length; i ++) {
            outputStream.write(source.read(base + i) & 0xFF);
        }
        return length;
    }

    public int dereferenceData() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int dataLength = copyTo(baos);
        updateSource(baos.toByteArray());
        return dataLength;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public MemorySection getSection() {
        return section;
    }

    public void setSection(MemorySection section) {
        this.section = section;
    }

    public Calibration copy() {
        Calibration copy = new Calibration();
        copy.setReadonly(isReadonly());
        copy.setName(getName());
        copy.setSection(getSection());
        copy.source = source;
        return copy;
    }

    public void apply(Calibration other) {
        this.setName(other.getName());
        this.setSection(other.getSection());
        this.setReadonly(other.isReadonly());
        if (other.source != null && other.source != source) {
            source = other.source;
        }
    }

    @Override
    public long getBaseAddress() {
        return section.getBaseAddress();
    }

    @Override
    public int getLength() {
        return source.getLength();
    }

    @Override
    public int read(byte[] dst, long memoryBase, int offs, int len) throws IOException {
        return source.read(dst, memoryBase, offs, len);
    }

    @Override
    public int read(long position) throws IOException {
        return source.read(position);
    }

    @Override
    public void write(byte[] bytes, long memoryBase, int offs, int len) throws IOException {
        if (isReadonly()) {
            throw new IOException(getName() + " is read-only");
        }

        source.write(bytes, memoryBase, offs, len);
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean isLocal() {
        return MemorySource.super.isLocal();
    }

    public static Calibration.Builder builder() {
        return new Calibration.Builder();
    }

    public static class Builder {
        private final Calibration calibration = new Calibration();

        public Calibration.Builder withName(String name) {
            calibration.setName(name);
            return this;
        }

        public Calibration.Builder withReadOnly(boolean readOnly) {
            calibration.setReadonly(readOnly);
            return this;
        }

        public Calibration.Builder withSection(MemorySection section) {
            calibration.setSection(section);
            return this;
        }

        public Calibration.Builder withSource(MemorySource source) {
            calibration.source = source;
            return this;
        }

        public Calibration build() {
            return calibration;
        }
    }
}
