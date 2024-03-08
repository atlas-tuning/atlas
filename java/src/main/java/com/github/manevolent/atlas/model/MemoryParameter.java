package com.github.manevolent.atlas.model;

public class MemoryParameter {
    private String name;
    private MemoryAddress address;
    private Scale scale;

    public Scale getScale() {
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

    public String toString() {
        return name + " (" + getScale().getFormat().name().toLowerCase() + ", " + address.toString() + ")";
    }

    public MemoryParameter copy() {
        MemoryParameter copy = new MemoryParameter();
        copy.name = name;
        copy.scale = scale;
        copy.address = address;
        return copy;
    }

    public void apply(MemoryParameter parameter) {
        this.name = parameter.name;
        this.scale = parameter.scale;
        this.address = parameter.address;
    }

    public static Builder builder() {
        return new Builder();
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
