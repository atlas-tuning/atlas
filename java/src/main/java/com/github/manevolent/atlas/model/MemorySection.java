package com.github.manevolent.atlas.model;

import com.github.manevolent.atlas.model.source.ArraySource;
import com.github.manevolent.atlas.model.source.VehicleSource;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HexFormat;

public class MemorySection {
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

    public MemoryAddress toBaseMemoryAddress() {
        return MemoryAddress.builder().withSection(this).withOffset(baseAddress).build();
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
            outputStream.write(source.read(baseAddress + i) & 0xFF);
        }
        return length;
    }

    /**
     * Sets up this memory section when a ROM/project is loaded
     * @param project ROM loaded
     * @param data data for this section
     */
    public void setup(Project project, byte[] data) {
        if (encryptionType != null && encryptionType != MemoryEncryptionType.NONE) {
            encryption = encryptionType.getFactory().create(project);
        } else {
            encryption = null;
        }

        if (this.source == null) {
            if (data != null) {
                source = new ArraySource(getBaseAddress(), data, 0, data.length);
            } else {
                source = new VehicleSource();
            }
        }
    }

    public int read(byte[] dst, long memoryOffs, int offs, int len) throws IOException {
        if (memoryOffs < baseAddress || memoryOffs + len >= baseAddress + dataLength) {
            throw new ArrayIndexOutOfBoundsException(Long.toString(memoryOffs));
        }

        if (encryption != null) {
            return encryption.read(source, memoryOffs, dst, offs, len);
        } else {
            return source.read(dst, memoryOffs, offs, len);
        }
    }

    public void write(byte[] src, long memoryOffs, int offs, int len) throws IOException {
        if (memoryOffs < baseAddress || memoryOffs + len >= baseAddress + dataLength) {
            throw new ArrayIndexOutOfBoundsException(Long.toString(memoryOffs));
        }

        if (encryption != null) {
            encryption.write(source, memoryOffs, src, offs, len);
        } else {
            source.write(src, memoryOffs, offs, len);
        }
    }

    public int read(long position) throws IOException {
        int readAddress = (int) (position - baseAddress);
        if (readAddress < 0 || readAddress >= dataLength) {
            throw new ArrayIndexOutOfBoundsException(readAddress);
        }

        return source.read(position);
    }

    public MemoryByteOrder getByteOrder() {
        return byteOrder;
    }

    public void setByteOrder(MemoryByteOrder byteOrder) {
        this.byteOrder = byteOrder;
    }

    @Override
    public String toString() {
        if (dataLength == 0) {
            return name + " [0x" + HexFormat.of().toHexDigits((int) baseAddress).toUpperCase() + "]";
        } else {
            return name + " [0x" + HexFormat.of().toHexDigits((int) baseAddress).toUpperCase() + "-" +
                    "0x" + HexFormat.of().toHexDigits((int) (baseAddress + dataLength)).toUpperCase() + "]";
        }
    }

    public MemorySection copy() {
        MemorySection copy = new MemorySection();
        copy.setDataLength(getDataLength());
        copy.setBaseAddress(getBaseAddress());
        copy.setName(getName());
        copy.setByteOrder(getByteOrder());
        copy.setMemoryType(getMemoryType());
        copy.setEncryptionType(getEncryptionType());
        return copy;
    }

    public void apply(MemorySection other) {
        setDataLength(other.getDataLength());
        setBaseAddress(other.getBaseAddress());
        setName(other.getName());
        setByteOrder(other.getByteOrder());
        setMemoryType(other.getMemoryType());
        setEncryptionType(other.getEncryptionType());
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean intersects(long baseAddress, int dataLength) {
        long otherStart = baseAddress;
        long otherEnd = baseAddress + dataLength;

        long myStart = this.baseAddress;
        long myEnd = this.baseAddress + this.dataLength;

        return (myStart <= otherEnd) && (myEnd >= otherStart);
    }

    public boolean intersects(MemorySection other) {
        return intersects(other.getBaseAddress(), other.getDataLength());
    }

    public boolean contains(MemoryAddress address) {
        return intersects(address.getOffset(), 0);
    }

    public boolean contains(MemoryReference reference) {
        return contains(reference.getAddress());
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
