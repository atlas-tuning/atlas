package com.github.manevolent.atlas.ssm4;

import com.github.manevolent.atlas.windows.CryptoAPI;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.RC2ParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.function.Function;

public class Crypto {
    private static final byte[] hashValue = new byte[] {
            (byte) 0xd4, (byte) 0xde, (byte) 0xe8, (byte) 0xdb,
            (byte) 0xcc, (byte) 0xdb, (byte) 0xd9, (byte) 0x92,
            (byte) 0x48, (byte) 0xb6, (byte) 0x9d, (byte) 0x88,
            (byte) 0xab, (byte) 0xae, (byte) 0xc0, (byte) 0x73,
            (byte) 0xbb, (byte) 0x8c, (byte) 0x84, (byte) 0x6f,
            (byte) 0x72, (byte) 0x7e, (byte) 0xb3, (byte) 0xc6,
            (byte) 0x8d, (byte) 0x74, (byte) 0xbc, (byte) 0xb4,
            (byte) 0xb7, (byte) 0xce, (byte) 0xe2, (byte) 0xe2,
            (byte) 0xff, (byte) 0x24, (byte) 0x1b, (byte) 0xbb,
            (byte) 0xe1, (byte) 0x32, (byte) 0x2f, (byte) 0x18
    };
    
    private static final byte[] ivValue = new byte[] {
            (byte) 0xa8, (byte) 0xb0, (byte) 0xc8, (byte) 0xc9,
            (byte) 0x6f, (byte) 0x9b, (byte) 0xaf, (byte) 0xb8,
            (byte) 0xbe, (byte) 0xc2, (byte) 0xc2, (byte) 0xa0,
            (byte) 0x89, (byte) 0x85, (byte) 0xb4, (byte) 0x8c,
    };

    public static byte[] computeSSM4EncryptionKey() {
        return computeEncryptionKey(hashValue, "SHA", 32);
    }

    public static byte[] computeEncryptionKey(byte[] key, String algorithm, int keyLength) {
        Function<byte[], byte[]> hashFunction = (data) -> {
            try {
                MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
                return messageDigest.digest(data);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        };

        return CryptoAPI.deriveAESKey(key, hashFunction, keyLength);
    }

    public static Cipher createCipher(int mode) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKey keySpec = new SecretKeySpec(computeSSM4EncryptionKey(), "AES");
        IvParameterSpec ivspec = new IvParameterSpec(ivValue);
        cipher.init(mode, keySpec, ivspec);
        return cipher;
    }

    public static Cipher createCipher(int mode, byte[] key, byte[] iv) throws GeneralSecurityException {
        return createCipher(mode, key, iv, "AES");
    }

    public static Cipher createCipher(int mode, byte[] key, byte[] iv, String instance) throws GeneralSecurityException {
        IvParameterSpec ivspec = null;
        if (iv != null) {
            ivspec = new IvParameterSpec(iv);
        }
        return createCipher(mode, key, ivspec, instance);
    }

    public static Cipher createCipher(int mode, byte[] key, AlgorithmParameterSpec param, String instance) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(instance);
        SecretKey keySpec = new SecretKeySpec(key, instance.split("/")[0]);
        cipher.init(mode, keySpec, param);
        return cipher;
    }

    public static byte[] decryptData(byte[] data) throws GeneralSecurityException {
        return createCipher(Cipher.DECRYPT_MODE).doFinal(data);
    }

    public static byte[] decryptData(byte[] data, byte[] key) throws GeneralSecurityException {
        return createCipher(Cipher.DECRYPT_MODE, key, null).doFinal(data);
    }

    public static long decryptFile(String source, String target, Runnable mkdirs) throws GeneralSecurityException, IOException {
        File sourceFile = new File(source);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (InputStream inputStream = new FileInputStream(sourceFile)) {
            inputStream.transferTo(baos);
        }

        byte[] decrypted;

        try {
            decrypted = decryptData(baos.toByteArray());
        } catch (IllegalBlockSizeException |BadPaddingException ex) {
            return 0;
        }

        long written;
        ByteArrayInputStream bais = new ByteArrayInputStream(decrypted);
        File targetFile = new File(target);
        mkdirs.run();
        try (OutputStream outputStream = new FileOutputStream(targetFile)) {
            written = bais.transferTo(outputStream);
        }

        System.out.println("Decrypted " + source + " => " + target);

        return written;
    }

    public static long decryptDirectory(File source, File target) throws GeneralSecurityException, IOException {
        long total = 0;
        File[] files = source.listFiles();
        if (files == null) {
            return 0;
        }

        for (File file : files) {
            if (file.getName().endsWith(".plain")) {
                continue;
            }

            if (file.isDirectory()) {
                total += decryptDirectory(file, new File(target.getAbsolutePath() + "/" + file.getName()));
            } else {
                total += decryptFile(file.getAbsolutePath(), target.getAbsolutePath() + "/" + file.getName(),
                        target::mkdirs);
            }
        }
        return total;
    }

    public static void main(String[] args) throws GeneralSecurityException, IOException {
        decryptDirectory(new File(args[0]), new File(args[1]));
    }


    public static byte[] toByteArray(String hex) {
        int len = hex.length();

        if (len % 2 != 0) {
            hex = "0" + hex;
            len ++;
        }

        byte[] ans = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            // using left shift operator on every character
            ans[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i+1), 16));
        }
        return ans;
    }

    public static void reverse(byte[] array) {
        if (array == null) {
            return;
        }
        int i = 0;
        int j = array.length - 1;
        byte tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }
}
