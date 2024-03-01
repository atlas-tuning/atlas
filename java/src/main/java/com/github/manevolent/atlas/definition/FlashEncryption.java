package com.github.manevolent.atlas.definition;

import java.io.IOException;

public interface FlashEncryption {

    void encrypt(byte[] data, int offs, int len) throws IOException;

    void decrypt(byte[] data, int offs, int len) throws IOException;

    int getBlockSize();

    default int read(FlashSource source, byte[] dst, int offs, int len) throws IOException {
        int blockStart = (int) Math.floor((double)offs / (double)getBlockSize());
        int blockEnd = (int) Math.floor((double)(offs+len) / (double)getBlockSize());
        int dataStart = blockStart * getBlockSize();
        int dataEnd = (blockEnd * getBlockSize()) + getBlockSize();
        byte[] cipherText = new byte[dataEnd - dataStart];
        int read = source.read(cipherText, dataStart, cipherText.length);
        if (read != cipherText.length) {
            throw new IOException("Unexpected read size: " + read + " != " + cipherText.length);
        }
        decrypt(cipherText, 0, cipherText.length);
        System.arraycopy(cipherText, offs - dataStart, dst, 0, len);
        return len;
    }

}
