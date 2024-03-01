package com.github.manevolent.atlas.windows;


import java.util.Arrays;
import java.util.function.Function;

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
    public static byte[] deriveKey(byte[] value, Function<byte[], byte[]> hashFunction, int keyLength) {
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

}
