package com.github.manevolent.atlas.ssm4;

public class SubaruDITFlashEncryption {

    private static byte[] feistel_lookup_table = new byte[] {
            (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x01, (byte) 0x09,
            (byte) 0x0c, (byte) 0x0d, (byte) 0x08, (byte) 0x0a, (byte) 0x0d,
            (byte) 0x02, (byte) 0x0b, (byte) 0x0f, (byte) 0x04, (byte) 0x00,
            (byte) 0x03, (byte) 0x0b, (byte) 0x04, (byte) 0x06, (byte) 0x00,
            (byte) 0x0f, (byte) 0x02, (byte) 0x0d, (byte) 0x09, (byte) 0x05,
            (byte) 0x0c, (byte) 0x01, (byte) 0x0a, (byte) 0x03, (byte) 0x0d,
            (byte) 0x0e, (byte) 0x08
    };

    public static short[] ENGINE_ECU_KEYS_ENCRYPTION = new short[] {
            (short)0x5fb1,
            (short)0xa7ca,
            (short)0x42da,
            (short)0xb740
    };

    public static short[] ENGINE_ECU_KEYS_DECRYPTION = new short[] {
            (short)0xb740,
            (short)0x42da,
            (short)0xa7ca,
            (short)0x5fb1,
    };

    public static void feistel_encrypt(int cleartext_symbol,
                                       byte[] data_out,
                                       short[] keys) {
        short uVar1;
        int uVar2;
        int uVar3;
        int uVar4;
        int iVar5;
        int uVar6;
        int local_c;

        byte[] abStack_8 = new byte[4];
        byte[] abStack_4 = new byte[4];

        uVar2 = 4;
        local_c = cleartext_symbol;

        int key_index = 0;
        do {
            uVar6 = uVar2;
            uVar4 = 0;
            uVar1 = keys[key_index];
            key_index = key_index + 1;
            uVar2 = (uVar1 ^ local_c) & 0xFFFF;
            uVar3 = uVar2 & 1;

            while( true ) {
                abStack_8[uVar4] = (byte)(uVar2 & 0x1f);
                uVar2 = (int)uVar2 >> 4;
                uVar4 = (uVar4 + 1) & 0xff;
                if (3 < uVar4) break;
                if ((uVar4 == 3) && (uVar3 == 1)) {
                    uVar2 = uVar2 | 0x10;
                }
            }

            uVar2 = 0;
            do {
                uVar3 = uVar2 + 1;
                abStack_4[uVar2] = feistel_lookup_table[abStack_8[uVar2]];
                uVar2 = uVar3;
            } while (uVar3 < 4);
            uVar2 = ((abStack_4[0] & 0xFF) +
                    ((abStack_4[1] & 0xFF) * 0x10) +
                    ((abStack_4[2] & 0xFF) * 0x100) +
                    ((abStack_4[3] & 0xFF) * 0x1000)) &
                    0xffff;

            iVar5 = 3;
            do {
                uVar3 = uVar2 & 1;
                uVar2 = uVar2 >> 1;
                if (uVar3 != 0) {
                    uVar2 = uVar2 | 0x8000;
                }
                iVar5 = iVar5 + -1;
            } while (iVar5 != 0);

            local_c = ((uVar2 ^ (local_c >> 0x10)) & 0xFFFF) | (local_c * 0x10000);

            uVar2 = uVar6 - 1;
        } while (uVar6 - 1 != 0);

        data_out[1] = (byte) ((local_c >> 16) & 0xFF);
        data_out[0] = (byte) ((local_c >> 24) & 0xFF);
        data_out[3] = (byte) ((local_c) & 0xFF);
        data_out[2] = (byte) ((local_c >> 8) & 0xFF);
    }

    /**
     * See: https://en.wikipedia.org/wiki/Feistel_cipher
     *
     * This is run for each 32-bit block of data desired to be decrypted.
     *
     * @param encrypted_symbol
     * @param
     */
    public static void feistel_decrypt(int encrypted_symbol,
                                       byte[] data_out,
                                       short[] keys) {
        short uVar1;
        int uVar2;
        int uVar3;
        int uVar4;
        int iVar5;
        int uVar6;
        int local_c;

        byte[] abStack_8 = new byte[4];
        byte[] abStack_4 = new byte[4];

        uVar2 = 4;
        local_c = encrypted_symbol;

        int key_index = 0;
        do {
            uVar6 = uVar2;
            uVar4 = 0;
            uVar1 = keys[key_index];
            key_index = key_index + 1;
            uVar2 = (uVar1 ^ (local_c >> 16)) & 0xFFFF;
            uVar3 = uVar2 & 1;

            while( true ) {
                abStack_8[uVar4] = (byte)(uVar2 & 0x1f);
                uVar2 = (int)uVar2 >> 4;
                uVar4 = (uVar4 + 1) & 0xff;
                if (3 < uVar4) break;
                if ((uVar4 == 3) && (uVar3 == 1)) {
                    uVar2 = uVar2 | 0x10;
                }
            }

            uVar2 = 0;
            do {
                uVar3 = uVar2 + 1;
                abStack_4[uVar2] = feistel_lookup_table[abStack_8[uVar2]];
                uVar2 = uVar3;
            } while (uVar3 < 4);
            uVar2 = ((abStack_4[0] & 0xFF) +
                    ((abStack_4[1] & 0xFF) * 0x10) +
                    ((abStack_4[2] & 0xFF) * 0x100) +
                    ((abStack_4[3] & 0xFF) * 0x1000)) &
                    0xffff;

            iVar5 = 3;
            do {
                uVar3 = uVar2 & 1;
                uVar2 = uVar2 >> 1;
                if (uVar3 != 0) {
                    uVar2 = uVar2 | 0x8000;
                }
                iVar5 = iVar5 + -1;
            } while (iVar5 != 0);

            local_c = (((uVar2 ^ (local_c)) & 0xFFFF) << 0x10) | ((local_c >> 0x10) & 0xFFFF);

            uVar2 = uVar6 - 1;
        } while (uVar6 - 1 != 0);

        data_out[1] = (byte) ((local_c >> 16) & 0xFF);
        data_out[0] = (byte) ((local_c >> 24) & 0xFF);
        data_out[3] = (byte) ((local_c) & 0xFF);
        data_out[2] = (byte) ((local_c >> 8) & 0xFF);
    }

}
