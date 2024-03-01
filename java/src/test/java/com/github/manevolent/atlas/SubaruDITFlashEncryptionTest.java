package com.github.manevolent.atlas;

import com.github.manevolent.atlas.ssm4.SubaruDITFlashEncryption;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.github.manevolent.atlas.ssm4.SubaruDITFlashEncryption.feistel_decrypt;
import static com.github.manevolent.atlas.ssm4.SubaruDITFlashEncryption.feistel_encrypt;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SubaruDITFlashEncryptionTest {

    @Test
    public void test_feistel_decrypt() {
        Map<Integer, Integer> expecteds = new LinkedHashMap<>();
        expecteds.put(0x98A49DE7, 0x00000000);
        expecteds.put(0x8AB4791C, 0x0A0A0A0A);
        expecteds.put(0xF135FA11, 0x0201FFFF);
        expecteds.put(0x393D667E, 0xEEEEEEEE);
        expecteds.put(0x941478D7, 0xFFFFFFFF);
    }

    @Test
    public void test_feistel_bidirectional() {
        for (int i = 0; i < 32; i ++) {
            int symbol = 0x0000001 << i;
            byte[] data = new byte[4];

            feistel_encrypt(symbol, data, SubaruDITFlashEncryption.ENGINE_ECU_KEYS_ENCRYPTION);

            ByteBuffer buffer = ByteBuffer.wrap(data);
            int encrypted_symbol = buffer.getInt();

            feistel_decrypt(encrypted_symbol, data, SubaruDITFlashEncryption.ENGINE_ECU_KEYS_DECRYPTION);

            buffer = ByteBuffer.wrap(data);
            int decrypted_symbol = buffer.getInt();

            assertEquals(Integer.toHexString(symbol), Integer.toHexString(decrypted_symbol));
        }
    }

    @Test
    public void test_feistel_encrypt() {
        Map<Integer, Integer> expecteds = new LinkedHashMap<>();
        expecteds.put(0x00000000, 0x98A49DE7);
        expecteds.put(0x0A0A0A0A, 0x8AB4791C);
        expecteds.put(0x0201FFFF, 0xF135FA11);
        expecteds.put(0xEEEEEEEE, 0x393D667E);
        expecteds.put(0xFFFFFFFF, 0x941478D7);

        for (Integer cleartext_symbol : expecteds.keySet()) {
            byte[] encrypted_data_out = new byte[4];
            SubaruDITFlashEncryption.feistel_encrypt(
                    cleartext_symbol,
                    encrypted_data_out,
                    SubaruDITFlashEncryption.ENGINE_ECU_KEYS_ENCRYPTION
            );

            ByteBuffer buffer = ByteBuffer.wrap(encrypted_data_out);
            int expected = expecteds.get(cleartext_symbol);
            assertEquals(
                    Integer.toHexString(expected),
                    Integer.toHexString(buffer.getInt()),
                    Integer.toHexString(cleartext_symbol) + " becomes " + Integer.toHexString(expected)
            );
        }
    }

}
