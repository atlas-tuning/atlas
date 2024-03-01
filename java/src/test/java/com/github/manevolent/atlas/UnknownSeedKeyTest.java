package com.github.manevolent.atlas;

import com.github.manevolent.atlas.ssm4.UnknownSeedKey;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UnknownSeedKeyTest {

    @Test
    public void testShift_1() throws Exception {
        Map<Integer, Integer> expected = new LinkedHashMap<>();
        expected.put(0x0, 0x0);
        expected.put(0xFF, 10454);
        expected.put(0xABAB, 10367);
        expected.put(0xFF00ABAB, 25719);
        expected.put(0x1, 170);
        expected.put(0x10, 2720);
        expected.put(0x100, 10624);
        expected.put(0x1000, 28715);
        expected.put(0x10000, 688);
        expected.put(0x100000, 15642);

        for (Integer seed : expected.keySet()) {
            int result = UnknownSeedKey.shift_1(seed);
            assertEquals(expected.get(seed), result, Integer.toHexString(seed));
        }
    }


    @Test
    public void testShift_2() throws Exception {
        Map<Integer, Integer> expected = new LinkedHashMap<>();

        expected.put(0x0, 0x0);
        expected.put(0xFF, 10901);
        expected.put(0xABAB, 24097);
        expected.put(0xFF00ABAB, -2259 & 0xFFFF);
        expected.put(0x1, 171);
        expected.put(0x10, 2736);
        expected.put(0x100, 11072);
        expected.put(0x1000, 3202);
        expected.put(0x10000, 30806);
        expected.put(0x100000, 9129);

        for (Integer seed : expected.keySet()) {
            int result = UnknownSeedKey.shift_2(seed);
            assertEquals(expected.get(seed), result, Integer.toHexString(seed));
        }
    }

    @Test
    public void testSeedKey_F() throws Exception {
        Map<byte[], Integer> expected = new LinkedHashMap<>();

        expected.put(
                new byte[] { 0x10, 0x20, 0x30, 0x40, 0x50, 0x60 },
                53284
        );

        expected.put(
                new byte[] { 0x50, 0x51, (byte)0xAA, (byte)0xB7, 0x00, 0x16 },
                15188
        );

        for (byte[] seed : expected.keySet()) {
            int result = UnknownSeedKey.scramble_function(seed, 6);
            assertEquals(expected.get(seed), result, Arrays.toString(seed));
        }
    }

    @Test
    public void testGenerateKey() {
        int result = UnknownSeedKey.generate_key(0x0, 0x0);
        assertEquals(result, 0x3210321);
    }

}
