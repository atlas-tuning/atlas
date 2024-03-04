package com.github.manevolent.atlas.definition;

import java.io.IOException;

public class Series {
    private String name;
    private int length;
    private FlashAddress address;
    private DataFormat format;
    private Unit unit;
    private Scale scale;

    public float get(int index) throws IOException {
        if (length > 0 && (index >= length || index < 0)) {
            throw new ArrayIndexOutOfBoundsException(index);
        }

        float data = address.read(index, format);
        return scale.forward(data);
    }

    public int get(float[] floats, int offs, int len) throws IOException {
        int i = 0;
        for (; i < len; i ++) {
            floats[offs + i] = get(i);
        }
        return i;
    }

    public float[] getNum(int numCells) throws IOException {
        float[] cells = new float[numCells];
        get(cells, 0, numCells);
        return cells;
    }

    public float[] getAll() throws IOException {
        return getNum(length);
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
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public Scale getScale() {
        return scale;
    }

    public void setScale(Scale scale) {
        this.scale = scale;
    }

    public DataFormat getFormat() {
        return format;
    }

    public void setFormat(DataFormat format) {
        this.format = format;
    }

    public FlashAddress getAddress() {
        return address;
    }

    public void setAddress(FlashAddress address) {
        this.address = address;
    }

    public static Builder builder() {
        return new Builder();
    }

    public float set(int index, float value) throws IOException {
        float data = scale.reverse(value);
        address.write(index, data, format);
        return get(index);
    }

    public static class Builder {
        private final Series series = new Series();

        public Builder() {
            series.setScale(Scale.ONE);
        }

        public Builder withName(String name) {
            this.series.setName(name);
            return this;
        }

        public Builder withAddress(FlashAddress address) {
            this.series.setAddress(address);
            return this;
        }

        public Builder withAddress(FlashRegion region, int offset) {
            return withAddress(FlashAddress.builder()
                    .withRegion(region)
                    .withOffset(offset)
                    .build());
        }

        public Builder withFormat(DataFormat format) {
            this.series.setFormat(format);
            return this;
        }

        public Builder withScale(Scale scale) {
            this.series.setScale(scale);
            if (scale.getUnit() != null && series.getUnit() == null) {
                withUnit(scale.getUnit());
            }
            if (scale.getFormat() != null && series.getFormat() == null) {
                withFormat(scale.getFormat());
            }
            return this;
        }

        public Builder withScale(Scale.Builder scale) {
            return withScale(scale.build());
        }

        public Builder withLength(int length) {
            this.series.setLength(length);
            return this;
        }

        public Builder withUnit(Unit unit) {
            this.series.setUnit(unit);
            return this;
        }

        public Series build() {
            if (series.address == null) {
                throw new NullPointerException("address");
            }

            if (series.format == null) {
                throw new NullPointerException("format");
            }

            if (series.scale == null) {
                throw new NullPointerException("scale");
            }

            if (series.length < 0) {
                throw new ArrayIndexOutOfBoundsException("length");
            }

            return series;
        }
    }
}
