package com.github.manevolent.atlas.definition;

import java.io.IOException;

public class FlashAddress {
    private FlashRegion region;
    private int offset;

    public FlashRegion getRegion() {
        return region;
    }

    public void setRegion(FlashRegion region) {
        this.region = region;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public float read(int index, DataFormat format) throws IOException {
        byte[] data = new byte[format.getSize()];
        getRegion().read(data, offset + (index * format.getSize()), format.getSize());
        return format.convertFromBytes(data);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final FlashAddress address;

        public Builder() {
            this.address = new FlashAddress();
        }

        public Builder withRegion(FlashRegion region) {
            address.setRegion(region);
            return this;
        }

        public Builder withOffset(int offset) {
            address.setOffset(offset);
            return this;
        }

        public FlashAddress build() {
            return address;
        }
    }
}
