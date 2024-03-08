package com.github.manevolent.atlas.model.subaru;

import com.github.manevolent.atlas.model.MemoryEncryption;
import com.github.manevolent.atlas.model.KeyProperty;
import com.github.manevolent.atlas.model.Rom;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class SubaruDITMemoryEncryption implements MemoryEncryption {
    private short[] encryptKey;
    private short[] decryptKey;

    public SubaruDITMemoryEncryption() {
    }

    @Override
    public void setEncryptionKeys(Rom rom) {
        KeyProperty property = rom.getProperty("subaru.dit.flashkey", KeyProperty.class);
        if (property == null) {
            throw new IllegalArgumentException("Missing key");
        } else if (property.getKey().length != 8) {
            throw new IllegalArgumentException("Invalid key length");
        }

        ByteBuffer buffer = ByteBuffer.wrap(property.getKey());
        encryptKey = new short[4];
        decryptKey = new short[4];
        encryptKey[0] = decryptKey[3] = buffer.getShort();
        encryptKey[1] = decryptKey[2] = buffer.getShort();
        encryptKey[2] = decryptKey[1] = buffer.getShort();
        encryptKey[3] = decryptKey[0] = buffer.getShort();
    }

    @Override
    public void encrypt(byte[] data, int offs, int len) throws IOException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data, offs, len);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            com.github.manevolent.atlas.ssm4.SubaruDITFlashEncryption.feistel_encrypt(inputStream, outputStream,
                    encryptKey);

            System.arraycopy(outputStream.toByteArray(), 0, data, 0, len);
        }
    }

    @Override
    public void decrypt(byte[] data, int offs, int len) throws IOException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data, offs, len);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            com.github.manevolent.atlas.ssm4.SubaruDITFlashEncryption.feistel_decrypt(inputStream, outputStream,
                    decryptKey);

            System.arraycopy(outputStream.toByteArray(), 0, data, 0, len);
        }
    }

    @Override
    public int getBlockSize() {
        return 32 / 8; // 32 bits
    }
}
