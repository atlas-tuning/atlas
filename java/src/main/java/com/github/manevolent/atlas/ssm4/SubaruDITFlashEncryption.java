package com.github.manevolent.atlas.ssm4;

/**
 * This is the flash encryption algorithm used for modules in the CAN network
 * for Subaru DIT ECUs, i.e. Renesas RH850-based MCUs such as the engine control
 * unit and so forth.
 *
 * The algorithm used is a Feistel cipher with a 32-bit block size and a 64-bit
 * key comprised of 4x16-bit "shorts". There is also a lookup/scramble table involved
 * for the 'F' function in Feistel. I go into more detail in the next paragraph.
 *
 * To learn more about Feistel, see: https://en.wikipedia.org/wiki/Feistel_cipher.
 *
 * In brief, Feistel divides the ciphertext/cleartext into 2 halves. In our case,
 * that is two 16-bit pieces. For encryption, one half is encrypted with a key ('K'),
 * and the halves are flipped. This is done 4 times for this particular implementation.
 * For decryption, the reverse occurs. This is possible as one half is always used as
 * a see for the other halves' key material. Furthermore, 'K' is always a list
 * of 4 keys, and each time a 'K' is retrieved from the key list, a function 'F' is
 * performed. In our case, this is a scrambling from a lookup table (feistel_lookup_table).
 *
 * The cipher used by these MCUs does not use chaining; each 32-bit
 * ciphertext word (which herein I call a "symbol") is unique to the cleartext
 * data and vice-versa. The same two 32-bit/word patterns always return the same ciphertext.
 * In this manner, the ciphertext may appear to repeat throughout a flash file (see: PK2
 * decryption in this repository).
 *
 * In order to decrypt an MCU you have such as a transmission control module, EyeSight,
 * etc., you first need to know the 64-bit key. You can obtain this by dumping ECU memory,
 * if the ECU offers that service. This service often requires an elevated security level.
 * For the keys for that, see: https://github.com/jglim/UnlockECU, an open repository where
 * the keys for each level are often easily discoverable. If you can't find the memory or
 * an example of cleartext ECU flash, you are out of luck, sadly.
 *
 * Once you have the binary data (.bin, etc.) of the ECU flash, you can pass it through
 * this cipher to decrypt the flash data or vice-versa.
 *
 */
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

    public static short[] ENGINE_ECU_KEYS_DECRYPTION = new short[] {
            (short)0x5fb1,
            (short)0xa7ca,
            (short)0x42da,
            (short)0xb740
    };

    public static short[] ENGINE_ECU_KEYS_ENCRYPTION = new short[] {
            (short)0xb740,
            (short)0x42da,
            (short)0xa7ca,
            (short)0x5fb1,
    };

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

        data_out[2] = (byte) ((local_c >> 24) & 0xFF);
        data_out[3] = (byte) ((local_c >> 16) & 0xFF);
        data_out[0] = (byte) ((local_c >> 8) & 0xFF);
        data_out[1] = (byte) ((local_c) & 0xFF);
    }

    public static void feistel_encrypt(int cleartext_symbol,
                                       byte[] data_out,
                                       short[] keys) {
        short low = (short) (cleartext_symbol & 0xFFFF);
        short high = (short) ((cleartext_symbol >> 16) & 0xFFFF);
        cleartext_symbol = (low << 16) | (high & 0xFFFF);

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
