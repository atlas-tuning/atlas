package com.github.manevolent.atlas.model;

import java.io.IOException;

public class Series {
    private String name;
    private int length;
    private MemoryAddress address;
    private Scale scale;

    public float get(MemorySource source, int index) throws IOException {
        if (length > 0 && (index >= length || index < 0)) {
            throw new ArrayIndexOutOfBoundsException(index);
        }

        float data = address.read(source, index, scale.getFormat());
        return scale.forward(data);
    }

    public int get(MemorySource source, float[] floats, int offs, int len) throws IOException {
        int i = 0;
        for (; i < len; i ++) {
            floats[offs + i] = get(source, i);
        }
        return i;
    }

    public float[] getNum(MemorySource source, int numCells) throws IOException {
        float[] cells = new float[numCells];
        get(source, cells, 0, numCells);
        return cells;
    }

    public float[] getAll(MemorySource source) throws IOException {
        return getNum(source, length);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public Unit getUnit() {
        return scale.getUnit();
    }

    public Scale getScale() {
        return scale;
    }

    public void setScale(Scale scale) {
        this.scale = scale;
    }

    public DataFormat getFormat() {
        return scale.getFormat();
    }

    public MemoryAddress getAddress() {
        return address;
    }

    public void setAddress(MemoryAddress address) {
        this.address = address;
    }

    public static Builder builder() {
        return new Builder();
    }

    public float set(MemorySource source, int index, float value) throws IOException {
        float data = scale.reverse(value);
        address.write(source, index, data, scale.getFormat());
        return get(source, index);
    }

    public Series copy() {
        Series copy = new Series();
        copy.scale = scale;
        copy.name = name;
        copy.length = length;
        copy.address = address;
        return copy;
    }

    public static class Builder {
        private final Series series = new Series();

        public Builder() {
            series.setScale(Scale.NONE);
        }

        public Builder withName(String name) {
            this.series.setName(name);
            return this;
        }

        public Builder withAddress(MemoryAddress address) {
            this.series.setAddress(address);
            return this;
        }

        public Builder withAddress(MemorySection section, int offset) {
            return withAddress(MemoryAddress.builder()
                    .withSection(section)
                    .withOffset(offset)
                    .build());
        }

        public Builder withScale(Scale scale) {
            this.series.setScale(scale);
            return this;
        }

        public Builder withScale(Scale.Builder scale) {
            return withScale(scale.build());
        }

        public Builder withLength(int length) {
            this.series.setLength(length);
            return this;
        }

        public Series build() {
            if (series.address == null) {
                throw new NullPointerException("address");
            }

            if (series.length < 0) {
                throw new ArrayIndexOutOfBoundsException("length");
            }

            return series;
        }
    }
}
