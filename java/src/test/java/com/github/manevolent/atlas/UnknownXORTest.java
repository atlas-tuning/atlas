package com.github.manevolent.atlas;

import org.junit.jupiter.api.Test;

import static com.github.manevolent.atlas.ssm4.UnknownXOR.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UnknownXORTest {

    @Test
    public void test_get_lookup_table_2_zeros() {
        int byte_offset = 0;
        byte[] DAT_febf8612 = new byte[4];
        byte[] DAT_febf8616 = new byte[4];
        byte DAT_febf8624 = 0x37;

        get_lookup_table_2(byte_offset, DAT_febf8612, DAT_febf8616, DAT_febf8624);

        assertEquals(0x19, DAT_febf8612[byte_offset]);
        assertEquals(0x37, DAT_febf8616[byte_offset]);
    }

    @Test
    public void test_CANDIDATE_xor_0() {
        System.out.println(Integer.toHexString(xor(0xD7)));
    }

}
