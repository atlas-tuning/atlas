package com.github.manevolent.atlas.model;

import java.io.IOException;
import java.util.HexFormat;

public class MemoryAddress {
    private MemorySection section;
    private long offset;

    public MemorySection getSection() {
        return section;
    }

    public void setSection(MemorySection section) {
        this.section = section;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "0x" + HexFormat.of().toHexDigits((int) offset).toUpperCase();
    }

    public float read(int index, DataFormat format) throws IOException {
        byte[] data = new byte[format.getSize()];
        getSection().read(data, offset + (index * format.getSize()), 0, format.getSize());
        return format.convertFromBytes(data, getSection().getByteOrder());
    }

    public static Builder builder() {
        return new Builder();
    }

    public void write(int index, float data, DataFormat format) throws IOException {
        byte[] bytes = format.convertToBytes(data, getSection().getByteOrder());
        getSection().write(bytes, offset + (index * format.getSize()), 0);
    }

    public static class Builder {
        private final MemoryAddress address;

        public Builder() {
            this.address = new MemoryAddress();
        }

        public Builder withSection(MemorySection section) {
            address.setSection(section);
            return this;
        }

        public Builder withOffset(long offset) {
            address.setOffset(offset);
            return this;
        }

        public MemoryAddress build() {
            return address;
        }
    }
}
