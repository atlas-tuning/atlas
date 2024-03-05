package com.github.manevolent.atlas.definition;

import java.io.IOException;
import java.util.HexFormat;

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

    @Override
    public String toString() {
        return "0x" + HexFormat.of().toHexDigits(offset).toUpperCase();
    }

    public float read(int index, DataFormat format) throws IOException {
        byte[] data = new byte[format.getSize()];
        getRegion().read(data, offset + (index * format.getSize()), 0, format.getSize());
        return format.convertFromBytes(data, getRegion().getByteOrder());
    }

    public static Builder builder() {
        return new Builder();
    }

    public void write(int index, float data, DataFormat format) throws IOException {
        byte[] bytes = format.convertToBytes(data, getRegion().getByteOrder());
        getRegion().write(bytes, offset + (index * format.getSize()), 0);
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
