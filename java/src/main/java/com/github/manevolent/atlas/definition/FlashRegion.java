package com.github.manevolent.atlas.definition;

import java.io.IOException;
import java.nio.ByteOrder;

public class FlashRegion implements FlashSource {
    private FlashType type;
    private FlashSource source;
    private FlashEncryption encryption;
    private ByteOrder byteOrder = ByteOrder.nativeOrder();
    private int baseAddress;
    private int dataLength;

    public FlashType getType() {
        return type;
    }

    public void setType(FlashType type) {
        this.type = type;
    }

    public FlashSource getSource() {
        return source;
    }

    public void setSource(FlashSource source) {
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

    public void setBaseAddress(int baseAddress) {
        this.baseAddress = baseAddress;
    }

    public int getDataLength() {
        return dataLength;
    }

    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
    }

    @Override
    public int read(byte[] dst, int flashOffs, int offs, int len) throws IOException {
        int readAddress = flashOffs - baseAddress;
        if (readAddress < 0 || readAddress >= dataLength) {
            throw new ArrayIndexOutOfBoundsException(readAddress);
        }

        if (encryption != null) {
            return encryption.read(getSource(), flashOffs, dst, offs, len);
        } else {
            return getSource().read(dst, flashOffs - baseAddress, offs, len);
        }
    }

    @Override
    public void write(byte[] src, int flashOffs, int offs, int len) throws IOException {
        int writeAddress = flashOffs - baseAddress;
        if (writeAddress < 0 || writeAddress >= dataLength) {
            throw new ArrayIndexOutOfBoundsException(writeAddress);
        }

        if (encryption != null) {
            encryption.write(getSource(), flashOffs, src, offs, len);
        } else {
            getSource().write(src, flashOffs - baseAddress, offs, len);
        }
    }

    @Override
    public int read(int offs) throws IOException {
        int readAddress = offs - baseAddress;
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
}
