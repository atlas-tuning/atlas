package com.github.manevolent.atlas;

import com.github.manevolent.atlas.ecc.MPC5746RECC;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MPC5746RECCTest {

    /**
     * From Freescale MPC5746R reference manual:
     *
     * Read address 0x00345678. Flash array returns fl_rdata = 0xFFFFFFFF_FFFFFFFF,
     * fl_cdata = 0xFF. The flash memory controller calculates the addr_chkbit = 0xC4
     * using the H-matrix; it inverts the fl_cdata vector to 0x00 and factors in the
     * addr_chkbit = 0xC4 to produce the following valid e2eECC codeword:
     * addr = 0x00345678, rdata = 0xFFFFFFFF_FFFFFFFF, rchkbit = 0xC4 (0x00 ^ 0xC4)
     */
    @Test
    public void test() {
        int addr = 0x00345678;
        long rdata = 0xFFFF_FFFF_FFFF_FFFFL; // 'L' for long here
        int rchkbit = MPC5746RECC.encodeEcc64(addr, rdata);
        assertEquals(0xC4, rchkbit);
    }

}
