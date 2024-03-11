package com.github.manevolent.atlas.model;

public class MemoryParameter {
    private String name;
    private MemoryAddress address;
    private Scale scale;
    private Color color;

    public Scale getScale() {
        if (scale == null) {
            return Scale.NONE;
        }

        return scale;
    }

    public void setScale(Scale scale) {
        this.scale = scale;
    }

    public MemoryAddress getAddress() {
        return address;
    }

    public void setAddress(MemoryAddress address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public String toString() {
        return name + " (" + getScale().getFormat().name().toLowerCase() + ", " + address.toString() + ")";
    }

    public MemoryParameter copy() {
        MemoryParameter copy = new MemoryParameter();
        copy.name = name;
        copy.scale = scale;
        copy.address = address;
        copy.color = color;
        return copy;
    }

    public void apply(MemoryParameter parameter) {
        this.name = parameter.name;
        this.scale = parameter.scale;
        this.address = parameter.address;
        this.color = parameter.color;
    }

    public static Builder builder() {
        return new Builder();
    }

    public byte[] newBuffer() {
        return new byte[getScale().getFormat().getSize()];
    }

    public float getValue(byte[] data) {
        float unscaled = getScale().getFormat().convertFromBytes(
                data,
                getAddress().getSection().getByteOrder().getByteOrder()
        );

        return getScale().forward(unscaled);
    }

    public static class Builder {
        private final MemoryParameter parameter = new MemoryParameter();

        public Builder withScale(Scale scale) {
            this.parameter.setScale(scale);
            return this;
        }

        public Builder withScale(Scale.Builder scale) {
            return withScale(scale.build());
        }

        public Builder withName(String name) {
            this.parameter.setName(name);
            return this;
        }

        public Builder withAddress(MemoryAddress address) {
            this.parameter.setAddress(address);
            return this;
        }

        public Builder withAddress(MemorySection section, int address) {
            this.parameter.setAddress(MemoryAddress.builder()
                    .withOffset(address)
                    .withSection(section)
                    .build());
            return this;
        }

        public MemoryParameter build() {
            return parameter;
        }
    }
}
