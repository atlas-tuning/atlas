package com.github.manevolent.atlas.definition;

import java.io.IOException;

public interface FlashEncryption {

    void encrypt(byte[] data, int offs, int len) throws IOException;

    void decrypt(byte[] data, int offs, int len) throws IOException;

    int getBlockSize();

    default int read(FlashSource source, int flashOffs, byte[] dst, int offs, int len) throws IOException {
        int blockStart = (int) Math.floor((double)flashOffs / (double)getBlockSize());
        int blockEnd = (int) Math.floor((double)(flashOffs+len) / (double)getBlockSize());
        int dataStart = blockStart * getBlockSize();
        int dataEnd = (blockEnd * getBlockSize()) + getBlockSize();
        byte[] cipherText = new byte[dataEnd - dataStart];
        int read = source.read(cipherText, dataStart, 0, cipherText.length);
        if (read != cipherText.length) {
            throw new IOException("Unexpected read size: " + read + " != " + cipherText.length);
        }
        decrypt(cipherText, 0, cipherText.length);
        System.arraycopy(cipherText, flashOffs - dataStart, dst, offs, len);
        return len;
    }

    default void write(FlashSource source, int flashOffs, byte[] src, int offs, int len) throws IOException {
        int blockStart = (int) Math.floor((double)flashOffs / (double)getBlockSize());
        int blockEnd = (int) Math.floor((double)(flashOffs+len) / (double)getBlockSize());
        int dataStart = blockStart * getBlockSize();
        int dataEnd = (blockEnd * getBlockSize()) + getBlockSize();
        byte[] data = new byte[dataEnd - dataStart];

        // Read the cleartext at this region
        source.read(data, dataStart, 0, data.length);
        decrypt(data, 0, data.length);

        // Update the cleartext
        System.arraycopy(src, offs, data, flashOffs - dataStart, len);

        // Encrypt the cleartext
        encrypt(data, 0, data.length);

        // Write the cleartext back
        source.write(data, dataStart, 0, data.length);
    }
}
