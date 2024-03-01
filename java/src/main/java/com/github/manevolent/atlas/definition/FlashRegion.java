package com.github.manevolent.atlas.definition;

import java.io.IOException;

public class FlashRegion implements FlashSource {
    private FlashType type;
    private FlashSource source;
    private FlashEncryption encryption;
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
    public int read(byte[] dst, int offs, int len) throws IOException {
        int readAddress = offs - baseAddress;
        if (readAddress < 0 || readAddress >= dataLength) {
            throw new ArrayIndexOutOfBoundsException(readAddress);
        }

        if (encryption != null) {
            return encryption.read(getSource(), dst, offs, len);
        } else {
            return getSource().read(dst, offs - baseAddress, len);
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
}
