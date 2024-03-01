package com.github.manevolent.atlas.windows;


import javax.crypto.Cipher;
import javax.crypto.spec.RC2ParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.function.Function;

import static com.github.manevolent.atlas.ssm4.Crypto.createCipher;

public class CryptoAPI {

    /**
     * A java implementation of the Windows CryptoAPI
     * 'CryptDeriveKey' function
     * Works for keys that aren't derived from the SHA-2 family and is either 3DES or AES
     *
     * See: https://stackoverflow.com/questions/29586097/how-to-export-aes-key-derived-using-cryptoapi/29589430#29589430
     *
     * @param value the value to hash
     * @param hashFunction hash function
     * @param keyLength desired key length
     * @return AES key
     */
    public static byte[] deriveAESKey(byte[] value, Function<byte[], byte[]> hashFunction, int keyLength) {
        byte[] hashValue1 = hashFunction.apply(value);

        byte[] buffer1 = new byte[64];
        Arrays.fill(buffer1, (byte)0x36);

        // Let k be the length of the hash value that is represented by the input
        int k = hashValue1.length;

        // Set the first k bytes of the buffer to the result of XOR with the
        // first k bytes of the buffer with the hash value that is represented by the input
        for (int n = 0; n < k; n ++) {
            buffer1[n] = (byte) (buffer1[n] ^ hashValue1[n]);
        }

        byte[] buffer2 = new byte[64];
        Arrays.fill(buffer2, (byte)0x5C);

        // Set the first k bytes of the buffer to the result of XOR with the
        // first k bytes of the buffer with the hash value that is represented by the input
        for (int n = 0; n < k; n ++) {
            buffer2[n] = (byte) (buffer2[n] ^ hashValue1[n]);
        }

        byte[] hashValueBuffer1 = hashFunction.apply(buffer1);
        byte[] hashValueBuffer2 = hashFunction.apply(buffer2);

        byte[] joinedBuffer = new byte[hashValueBuffer1.length + hashValueBuffer2.length];
        System.arraycopy(hashValueBuffer1, 0, joinedBuffer, 0, hashValueBuffer1.length);
        System.arraycopy(hashValueBuffer2, 0, joinedBuffer,  hashValueBuffer1.length, hashValueBuffer2.length);

        byte[] key = new byte[keyLength];
        System.arraycopy(joinedBuffer, 0, key, 0, keyLength);
        return key;
    }

    public static byte[] deriveRC2Key(String keyword, String hashAlgorithm) throws NoSuchAlgorithmException {
        return deriveRC2Key(keyword.getBytes(StandardCharsets.US_ASCII), hashAlgorithm);
    }

    public static final int RC2_KEY_LENGTH = 40 / 8; // 40 bits
    public static final int RC2_EFFECTIVE_KEY_BITS = 40; // 40 bits
    public static final byte[] RC2_SALT = new byte[11]; // Eleven 0's as per MSDN
    public static final byte[] RC2_IV = new byte[8]; // Eight 0's as per MSDN
    public static final String RC2_ALGORITHM = "RC2/CBC/PKCS5Padding"; // As per S/O
    public static final int RC2_BLOCK_LENGTH = 64 / 8; // 64 bits

    /**
     * This is a bit different from getting an AES/3DES key.
     *
     * See: https://stackoverflow.com/questions/76871293/rc2-decryption-from-wincrypt-api-to-go
     *
     * Thanks so much to the S.O. user for clarifying the 11-byte legacy salting piece.
     *
     * @return RC2 key; use with 8-byte IV set to 0x00's
     */
    public static byte[] deriveRC2Key(byte[] keyword, String hashAlgorithm) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance(hashAlgorithm);
        byte[] hash = md5.digest(keyword);

        byte[] finalKey = new byte[16];

        System.arraycopy(hash, 0, finalKey, 0, RC2_KEY_LENGTH);
        System.arraycopy(RC2_SALT, 0, finalKey, 5, RC2_SALT.length); // This is a no-op, but for clarity's sake

        return finalKey;
    }

    /**
     * Creates a windows crypto API friendly Java RC2 instance
     * @param keyword ASCII keyword to use to generate cipher
     * @return
     * @throws GeneralSecurityException
     */
    public static Cipher createRC2(String keyword) throws GeneralSecurityException {
        return createCipher(
                Cipher.DECRYPT_MODE,
                deriveRC2Key(keyword, "MD5"),
                new RC2ParameterSpec(RC2_EFFECTIVE_KEY_BITS, CryptoAPI.RC2_IV),
                CryptoAPI.RC2_ALGORITHM
        );
    }

}
