package com.github.manevolent.atlas;

import com.github.manevolent.atlas.ssm4.Crypto;
import com.github.manevolent.atlas.ssm4.AES;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SSM4AESTest {
    /**
     * Tests with full zero memory to check against binary instruction data
     */
    @Test
    public void testAES_Zeros() {
        byte[] expected = {(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
                (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
                (byte)0x00,

                (byte)0x62,(byte)0x63,(byte)0x63,(byte)0x63,(byte)0x62,
                (byte)0x63,(byte)0x63,(byte)0x63,(byte)0x62,(byte)0x63,(byte)0x63,(byte)0x63,(byte)0x62,
                (byte)0x63,(byte)0x63,(byte)0x63,(byte)0x9b,(byte)0x98,(byte)0x98,(byte)0xc9,(byte)0xf9,
                (byte)0xfb,(byte)0xfb,(byte)0xaa,(byte)0x9b,(byte)0x98,(byte)0x98,(byte)0xc9,(byte)0xf9,
                (byte)0xfb,(byte)0xfb,(byte)0xaa,(byte)0x90,(byte)0x97,(byte)0x34,(byte)0x50,(byte)0x69,
                (byte)0x6c,(byte)0xcf,(byte)0xfa,(byte)0xf2,(byte)0xf4,(byte)0x57,(byte)0x33,(byte)0x0b,
                (byte)0x0f,(byte)0xac,(byte)0x99,(byte)0xee,(byte)0x06,(byte)0xda,(byte)0x7b,(byte)0x87,
                (byte)0x6a,(byte)0x15,(byte)0x81,(byte)0x75,(byte)0x9e,(byte)0x42,(byte)0xb2,(byte)0x7e,
                (byte)0x91,(byte)0xee,(byte)0x2b,(byte)0x7f,(byte)0x2e,(byte)0x2b,(byte)0x88,(byte)0xf8,
                (byte)0x44,(byte)0x3e,(byte)0x09,(byte)0x8d,(byte)0xda,(byte)0x7c,(byte)0xbb,(byte)0xf3,
                (byte)0x4b,(byte)0x92,(byte)0x90,(byte)0xec,(byte)0x61,(byte)0x4b,(byte)0x85,(byte)0x14,
                (byte)0x25,(byte)0x75,(byte)0x8c,(byte)0x99,(byte)0xff,(byte)0x09,(byte)0x37,(byte)0x6a,
                (byte)0xb4,(byte)0x9b,(byte)0xa7,(byte)0x21,(byte)0x75,(byte)0x17,(byte)0x87,(byte)0x35,
                (byte)0x50,(byte)0x62,(byte)0x0b,(byte)0xac,(byte)0xaf,(byte)0x6b,(byte)0x3c,(byte)0xc6,
                (byte)0x1b,(byte)0xf0,(byte)0x9b,(byte)0x0e,(byte)0xf9,(byte)0x03,(byte)0x33,(byte)0x3b,
                (byte)0xa9,(byte)0x61,(byte)0x38,(byte)0x97,(byte)0x06,(byte)0x0a,(byte)0x04,(byte)0x51,
                (byte)0x1d,(byte)0xfa,(byte)0x9f,(byte)0xb1,(byte)0xd4,(byte)0xd8,(byte)0xe2,(byte)0x8a,
                (byte)0x7d,(byte)0xb9,(byte)0xda,(byte)0x1d,(byte)0x7b,(byte)0xb3,(byte)0xde,(byte)0x4c,
                (byte)0x66,(byte)0x49,(byte)0x41,(byte)0xb4,(byte)0xef,(byte)0x5b,(byte)0xcb,(byte)0x3e,
                (byte)0x92,(byte)0xe2,(byte)0x11,(byte)0x23,(byte)0xe9,(byte)0x51,(byte)0xcf,(byte)0x6f,
                (byte)0x8f,(byte)0x18,(byte)0x8e};

        byte[] param1 = new byte[16]; // zeros
        int flag = 0x10;
        byte[] edi = new byte[0xFF]; // zeros
        int res = AES.keyExpansion(param1, flag, edi, (offs, data) -> {
            assertEquals(expected[offs], data, "offs " + offs);
        });

        assertEquals(0, res);

        byte[] expected_aes = { (byte)0x66, (byte)0xe9, (byte)0x4b, (byte)0xd4, (byte)0xef,
                (byte)0x8a, (byte)0x2c, (byte)0x3b, (byte)0x88, (byte)0x4c, (byte)0xfa,
                (byte)0x59, (byte)0xca, (byte)0x34, (byte)0x2b, (byte)0x2e };
        byte[] result = new byte[0x10]; // zeros
        byte[] param_1 = new byte[16]; // zeros
        res = AES.aes(param_1, edi, result);

        assertEquals(0, res);
        assertArrayEquals(expected_aes, result);
    }

    /**
     * This test incorporates real captured flash data from a 2022 WRX using stock firmware
     */
    @Test
    public void testAES_RealWorldData() {
        byte[] key_7_8 = Crypto.toByteArray("7692E7932F23A901568DDFA5FF580625");
        byte[] key_1_2 = Crypto.toByteArray("667E3078219976B4EDF3D43BD1D8FFC9");

        byte[][][] challenges = {
                { // 7->8
                    Crypto.toByteArray("B1022DCC10E7040E35496ABD42165C44"),
                        Crypto.toByteArray("2F366613C54B7DD4E3A5FAF928C520CA"),
                        key_7_8
                },

                { //1->2
                    Crypto.toByteArray("267602ADCB385903ABB498A8DC114BF2"),
                        Crypto.toByteArray("094CCC38578FF11E9794822A8B0F6275"),
                        key_1_2
                },

                { //7->8
                    Crypto.toByteArray("4E6E90E986D86F12A7E0D1A223816FB6"),
                        Crypto.toByteArray("0C8733B10F8DA0C3B757881F689546D8"),
                        key_7_8
                },

                { //7->8
                        Crypto.toByteArray("5F344310801120D5B1D3CF12CB267188"),
                        Crypto.toByteArray("794F12E608FD0508329B2C42E5872E4F"),
                        key_7_8
                }
        };

        // Run through all the captured challenges and make sure we have the right keys
        for (byte[][] challenge_pair : challenges) {
            byte[] seed = challenge_pair[0];
            byte[] solution = challenge_pair[1];
            byte[] key = challenge_pair[2];
            byte[] our_solution = AES.answer(key, seed);
            if (!Arrays.equals(solution, our_solution)) {
                throw new AssertionError("Failed to solve " + Frame.toHexString(seed));
            }
        }
    }

}
