package com.github.manevolent.atlas.model;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.HexFormat;

public class MemorySection implements MemorySource {
    private String name;
    private FlashType type;
    private MemorySource source;
    private FlashEncryption encryption;
    private ByteOrder byteOrder = ByteOrder.nativeOrder();
    private long baseAddress;
    private int dataLength;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FlashType getType() {
        return type;
    }

    public void setType(FlashType type) {
        this.type = type;
    }

    public MemorySource getSource() {
        return source;
    }

    public void setSource(MemorySource source) {
        this.source = source;
    }

    public FlashEncryption getEncryption() {
        return encryption;
    }

    public void setEncryption(FlashEncryption encryption) {
        this.encryption = encryption;
    }

    public long getBaseAddress() {
        return baseAddress;
    }

    public void setBaseAddress(long baseAddress) {
        this.baseAddress = baseAddress;
    }

    public int getDataLength() {
        return dataLength;
    }

    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
    }

    @Override
    public int read(byte[] dst, long flashOffs, int offs, int len) throws IOException {
        int readAddress = (int) (flashOffs - baseAddress);
        if (readAddress < 0 || readAddress + len >= dataLength) {
            throw new ArrayIndexOutOfBoundsException(readAddress);
        }

        if (encryption != null) {
            return encryption.read(getSource(), flashOffs, dst, offs, len);
        } else {
            return getSource().read(dst, flashOffs - baseAddress, offs, len);
        }
    }

    @Override
    public void write(byte[] src, long flashOffs, int offs, int len) throws IOException {
        int writeAddress = (int) (flashOffs - baseAddress);
        if (writeAddress < 0 || writeAddress + len >= dataLength) {
            throw new ArrayIndexOutOfBoundsException(writeAddress);
        }

        if (encryption != null) {
            encryption.write(getSource(), flashOffs, src, offs, len);
        } else {
            getSource().write(src, flashOffs - baseAddress, offs, len);
        }
    }

    @Override
    public int read(long flashOffs) throws IOException {
        int readAddress = (int) (flashOffs - baseAddress);
        if (readAddress < 0 || readAddress >= dataLength) {
            throw new ArrayIndexOutOfBoundsException(readAddress);
        }

        return getSource().read(readAddress);
    }

    public ByteOrder getByteOrder() {
        return byteOrder;
    }

    public void setByteOrder(ByteOrder byteOrder) {
        this.byteOrder = byteOrder;
    }

    @Override
    public String toString() {
        return name + " [0x" + HexFormat.of().toHexDigits((int) baseAddress).toUpperCase() + "-" +
                "0x" + HexFormat.of().toHexDigits((int) (baseAddress + dataLength - 1)).toUpperCase() + "]";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final MemorySection section = new MemorySection();

        public Builder withName(String name) {
            section.setName(name);
            return this;
        }

        public Builder withByteOrder(ByteOrder name) {
            section.setByteOrder(name);
            return this;
        }

        public Builder withBaseAddress(long baseAddress) {
            section.setBaseAddress(baseAddress);
            return this;
        }

        public Builder withLength(int length) {
            section.setDataLength(length);
            return this;
        }

        public Builder withEndAddress(long endAddress) {
            section.setDataLength((int) (endAddress - section.getBaseAddress()));
            return this;
        }

        public Builder withSource(MemorySource source) {
            section.setSource(source);
            return this;
        }

        public Builder withEncryption(FlashEncryption encryption) {
            section.setEncryption(encryption);
            return this;
        }

        public MemorySection build() {
            return section;
        }
    }
}
