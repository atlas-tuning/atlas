package com.github.manevolent.atlas.definition.subaru;

import com.github.manevolent.atlas.definition.FlashEncryption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SubaruDITFlashEncryption implements FlashEncryption {
    public static SubaruDITFlashEncryption WRX_MT_2022_USDM = new SubaruDITFlashEncryption(
            com.github.manevolent.atlas.ssm4.SubaruDITFlashEncryption.ENGINE_ECU_KEYS_ENCRYPTION,
            com.github.manevolent.atlas.ssm4.SubaruDITFlashEncryption.ENGINE_ECU_KEYS_DECRYPTION
    );

    private final short[] encrypt;
    private final short[] decrypt;

    public SubaruDITFlashEncryption(short[] encrypt, short[] decrypt) {
        this.encrypt = encrypt;
        this.decrypt = decrypt;
    }

    @Override
    public void encrypt(byte[] data, int offs, int len) throws IOException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data, offs, len);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            com.github.manevolent.atlas.ssm4.SubaruDITFlashEncryption.feistel_encrypt(inputStream, outputStream,
                    encrypt);

            System.arraycopy(outputStream.toByteArray(), 0, data, 0, len);
        }
    }

    @Override
    public void decrypt(byte[] data, int offs, int len) throws IOException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data, offs, len);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            com.github.manevolent.atlas.ssm4.SubaruDITFlashEncryption.feistel_decrypt(inputStream, outputStream,
                    decrypt);

            System.arraycopy(outputStream.toByteArray(), 0, data, 0, len);
        }
    }

    @Override
    public int getBlockSize() {
        return 32 / 8; // 32 bits
    }
}
