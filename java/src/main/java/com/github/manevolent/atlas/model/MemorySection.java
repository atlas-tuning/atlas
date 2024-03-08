package com.github.manevolent.atlas.model;

import com.github.manevolent.atlas.model.source.ArraySource;
import com.github.manevolent.atlas.model.source.VehicleSource;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HexFormat;

public class MemorySection implements MemorySource {
    private String name;
    private MemoryType memoryType;
    private MemorySource source;
    private MemoryEncryptionType encryptionType;
    private MemoryEncryption encryption;
    private MemoryByteOrder byteOrder;
    private long baseAddress;
    private int dataLength;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MemoryType getMemoryType() {
        return memoryType;
    }

    public void setMemoryType(MemoryType memoryType) {
        this.memoryType = memoryType;
    }

    public void setSource(MemorySource source) {
        this.source = source;
    }

    public MemoryEncryptionType getEncryptionType() {
        return encryptionType;
    }

    public void setEncryptionType(MemoryEncryptionType type) {
        this.encryptionType = type;
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

    public int copyTo(OutputStream outputStream) throws IOException {
        int length = getDataLength();
        for (int i = 0; i < length; i ++) {
            outputStream.write(read(getBaseAddress() + i));
        }
        return length;
    }

    public void setup(Rom rom, byte[] data) {
        if (encryptionType != null && encryptionType != MemoryEncryptionType.NONE) {
            encryption = encryptionType.create(rom);
        } else {
            encryption = null;
        }

        if (data != null) {
            source = new ArraySource(data, 0, data.length);
        } else {
            source = new VehicleSource();
        }
    }

    @Override
    public int read(byte[] dst, long memoryOffs, int offs, int len) throws IOException {
        int readAddress = (int) (memoryOffs - baseAddress);
        if (readAddress < 0 || readAddress + len >= dataLength) {
            throw new ArrayIndexOutOfBoundsException(readAddress);
        }

        if (encryption != null) {
            return encryption.read(source, memoryOffs, dst, offs, len);
        } else {
            return source.read(dst, memoryOffs - baseAddress, offs, len);
        }
    }

    @Override
    public void write(byte[] src, long memoryOffs, int offs, int len) throws IOException {
        int writeAddress = (int) (memoryOffs - baseAddress);
        if (writeAddress < 0 || writeAddress + len >= dataLength) {
            throw new ArrayIndexOutOfBoundsException(writeAddress);
        }

        if (encryption != null) {
            encryption.write(source, memoryOffs, src, offs, len);
        } else {
            source.write(src, memoryOffs - baseAddress, offs, len);
        }
    }

    @Override
    public int read(long memoryOffs) throws IOException {
        int readAddress = (int) (memoryOffs - baseAddress);
        if (readAddress < 0 || readAddress >= dataLength) {
            throw new ArrayIndexOutOfBoundsException(readAddress);
        }

        return source.read(readAddress - memoryOffs);
    }

    public MemoryByteOrder getByteOrder() {
        return byteOrder;
    }

    public void setByteOrder(MemoryByteOrder byteOrder) {
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

        public Builder withByteOrder(MemoryByteOrder byteOrder) {
            section.setByteOrder(byteOrder);
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

        public Builder withEncryptionType(MemoryEncryptionType type) {
            section.setEncryptionType(type);
            return this;
        }

        public Builder withType(MemoryType type) {
            section.setMemoryType(type);
            return this;
        }

        public MemorySection build() {
            return section;
        }
    }
}
