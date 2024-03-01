package com.github.manevolent.atlas;

import com.github.manevolent.atlas.ssm4.Crypto;

import com.github.manevolent.atlas.windows.CryptoAPI;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.RC2ParameterSpec;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;

import static com.github.manevolent.atlas.ssm4.Crypto.createCipher;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CryptoAPITest {

    @Test
    public void testRC2() throws GeneralSecurityException {
        String cipherText = "596914c6020c9dbfc193e9e588a380730add8e1a69a4994ec57362dd1ad1d37a00932a64a385af6d8c5234b0c36d50c4";
        byte[] cipherBytes = Crypto.toByteArray(cipherText);
        assertEquals(cipherText, Frame.toHexString(cipherBytes).toLowerCase());
        String keyword = "some test key";
        byte[] key = CryptoAPI.deriveRC2Key(keyword, "MD5");
        byte[] expectedKey = Crypto.toByteArray("9bf00ebbb40000000000000000000000");
        assertArrayEquals(expectedKey, key);

        Cipher rc2 = CryptoAPI.createRC2(keyword);
        assertEquals(CryptoAPI.RC2_BLOCK_LENGTH, rc2.getBlockSize());

        byte[] clearBytes = rc2.doFinal(cipherBytes);
        String clearText = new String(clearBytes);
        assertEquals(
                "The quick brown fox jumps over the lazy dog",
                clearText
        );
    }

}
