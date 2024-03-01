package com.github.manevolent.atlas.ssm4;

public class UnknownXOR {

    private static byte[] lookup_table = new byte[] {
            (byte)0x79,
            (byte)0x0a,
            (byte)0x15,
            (byte)0x2b,
            (byte)0x57,
            (byte)0x56,
            (byte)0x54,
            (byte)0x50,
            (byte)0x58,
            (byte)0x48,
            (byte)0x68,
            (byte)0x28,
            (byte)0x51,
            (byte)0x5a,
            (byte)0x4c,
            (byte)0x60,
            (byte)0x38,
            (byte)0x71,
            (byte)0x1a,
            (byte)0x35,
            (byte)0x6b,
            (byte)0x2e,
            (byte)0x5d,
            (byte)0x42,
            (byte)0x7c,
            (byte)0x00,
            (byte)0x01,
            (byte)0x03,
            (byte)0x07,
            (byte)0x0f,
            (byte)0x1f,
            (byte)0x3f,
            (byte)0x7f,
            (byte)0x06,
            (byte)0x0d,
            (byte)0x1b,
            (byte)0x37,
            (byte)0x6f,
            (byte)0x26,
            (byte)0x4d,
            (byte)0x62,
            (byte)0x3c,
            (byte)0x00
    };

    private static byte[] lookup_table_2 = new byte[] {
            (byte)0x00,
            (byte)0x8a,
            (byte)0xd1,
            (byte)0xb8,
            (byte)0x6a,
            (byte)0x43,
            (byte)0xce,
            (byte)0x59,
            (byte)0xfa,
            (byte)0x31,
            (byte)0x2a,
            (byte)0x1c,
            (byte)0x70,
            (byte)0x77,
            (byte)0xa6,
            (byte)0x89,
            (byte)0xd7,
            (byte)0x6b,
            (byte)0x41,
            (byte)0xca,
            (byte)0x8e,
            (byte)0x06,
            (byte)0x44,
            (byte)0x1f,
            (byte)0x76,
            (byte)0xa4,
            (byte)0x8d,
            (byte)0x00,
            (byte)0x97,
            (byte)0x34,
            (byte)0xff,
            (byte)0xe4,
            (byte)0xd2,
            (byte)0xbe,
            (byte)0xb9,
            (byte)0x68,
            (byte)0x47,
            (byte)0x19,
            (byte)0xa5,
            (byte)0x8f,
            (byte)0x04,
            (byte)0x40,
            (byte)0xc8,
            (byte)0x8d,
            (byte)0x8a,
            (byte)0xd1,
            (byte)0xb8,
            (byte)0x6a,
            (byte)0x43,
            (byte)0xce,
            (byte)0x59,
            (byte)0xfa,
            (byte)0x31,
            (byte)0x2a,
            (byte)0x1c,
            (byte)0x70,
            (byte)0x77,
            (byte)0xa6,
            (byte)0x89,
            (byte)0xd7,
            (byte)0x6b,
            (byte)0x41,
            (byte)0xca,
            (byte)0x8e,
            (byte)0x06,
            (byte)0x44,
            (byte)0x1f,
            (byte)0x76,
            (byte)0xa4,
            (byte)0x8d,
            (byte)0x00,
            (byte)0x97,
            (byte)0x34,
            (byte)0xff,
            (byte)0xe4,
            (byte)0xd2,
            (byte)0xbe,
            (byte)0xb9,
            (byte)0x68,
            (byte)0x47,
            (byte)0x19,
            (byte)0xa5,
            (byte)0x8f,
            (byte)0x04,
            (byte)0x40,
            (byte)0xc8,
            (byte)0x8d,
            (byte)0x8a,
            (byte)0xd1,
            (byte)0xb8,
            (byte)0x6a,
            (byte)0x43,
            (byte)0xce,
            (byte)0x59,
            (byte)0xfa,
            (byte)0x31,
            (byte)0x2a,
            (byte)0x1c,
            (byte)0x70,
            (byte)0x77,
            (byte)0xa6,
            (byte)0x89,
            (byte)0xd7,
            (byte)0x6b,
            (byte)0x41,
            (byte)0xca,
            (byte)0x8e,
            (byte)0x06,
            (byte)0x44,
            (byte)0x1f,
            (byte)0x76,
            (byte)0xa4,
            (byte)0x8d,
            (byte)0x00,
            (byte)0x97,
            (byte)0x34,
            (byte)0xff,
            (byte)0xe4,
            (byte)0xd2,
            (byte)0xbe,
            (byte)0xb9,
            (byte)0x68,
            (byte)0x47,
            (byte)0x19,
            (byte)0xa5,
            (byte)0x8f,
            (byte)0x04,
            (byte)0x40,
            (byte)0xc8,
            (byte)0x8d,
            (byte)0x8a,
            (byte)0xd1,
            (byte)0xb8,
            (byte)0x6a,
            (byte)0x43,
            (byte)0xce,
            (byte)0x59,
            (byte)0xfa,
            (byte)0x31,
            (byte)0x2a,
            (byte)0x1c,
            (byte)0x70,
            (byte)0x77,
            (byte)0xa6,
            (byte)0x89,
            (byte)0xd7,
            (byte)0x6b,
            (byte)0x41,
            (byte)0xca,
            (byte)0x8e,
            (byte)0x06,
            (byte)0x44,
            (byte)0x1f,
            (byte)0x76,
            (byte)0xa4,
            (byte)0x8d,
            (byte)0x00,
            (byte)0x97,
            (byte)0x34,
            (byte)0xff,
            (byte)0xe4,
            (byte)0xd2,
            (byte)0xbe,
            (byte)0xb9,
            (byte)0x68,
            (byte)0x47,
            (byte)0x19,
            (byte)0xa5,
            (byte)0x8f,
            (byte)0x04,
            (byte)0x40,
            (byte)0xc8,
            (byte)0x8d,
            (byte)0x00,
            (byte)0x00,
            (byte)0x00
};

    public static void get_lookup_table_1(int byte_offset) {
        /**
         *                              **************************************************************
         *                              *                          FUNCTION                          *
         *                              **************************************************************
         *                              undefined FUN_0010429e()
         *              undefined         r10:1          <RETURN>
         *                              FUN_0010429e                                    XREF[4]:     CANDIDATE_xor_0:00104314(c),
         *                                                                                           CANDIDATE_xor_1:0010434a(c),
         *                                                                                           CANDIDATE_xor_2:00104380(c),
         *                                                                                           CANDIDATE_xor_3:001043b6(c)
         *         0010429e 86 00           zxb        r6               R6 becomes a 4-byte int, simple zero extension
         *         001042a0 40 0e 10 00     movhi      0x10,r0,r1
         *         001042a4 3e 06 16        mov        0xfebf8616,ep
         *                  86 bf fe
         *         001042aa 81 0f bd 21     ld.bu      0x21bc[r1],r1=>DAT_001021bc
         *         001042ae c6 f1           add        r6,ep
         *         001042b0 60 08           sld.bu     0x0[ep],r1=>DAT_febf8616_lookup_value_offs0      = 19h
         *         001042b2 3e 06 12        mov        0xfebf8612,ep
         *                  86 bf fe
         *         001042b8 c6 f1           add        r6,ep
         *         001042ba 60 08           sld.bu     0x0[ep],r1=>DAT_febf8612_lookup_value_2_offs0    = 19h
         *         001042bc 7f 00           jmp        [lp]
         */




    }

    public static void get_lookup_table_2(int byte_offset,
                                          byte[] DAT_febf8612,
                                          byte[] DAT_febf8616,
                                          byte DAT_febf8624) {
        int index2;
        int index;
        byte lookup_value;

        byte_offset = byte_offset & 0xff;
        index2 = 0;
        index = 0;

        do {
            lookup_value = lookup_table[index];
            index2 = index2 + 1 & 0xff;
            if (lookup_value == DAT_febf8624)
                break;
            index = index + 1;
        } while (index < 0x2b);

        DAT_febf8612[byte_offset] = lookup_table_2[(short)byte_offset * 0x2b + index2];
        DAT_febf8616[byte_offset] = lookup_value;
    }

    public static void CANDIDATE_xor_0(int byte_offset, byte[] DAT_febf8612, byte[] DAT_febf8616, byte DAT_febf8624)
    {
        // r6 = byte_offset
        //
        get_lookup_table_1(byte_offset); // seems to do nothing?
        get_lookup_table_2(byte_offset, DAT_febf8612, DAT_febf8616, DAT_febf8624);

        DAT_febf8616[byte_offset] = xor(DAT_febf8616[byte_offset]);
    }

    public static byte xor(int uVar1) {
        int iVar2;
        int uVar3;
        int uVar4;

        uVar4 = 1;
        iVar2 = 8;
        do {
            uVar3 = uVar1 ^ uVar4;
            uVar1 = uVar1 << 1;
            uVar4 = uVar4 << 1;
            if ((uVar3 & 0x80) != 0) {
                uVar1 = uVar1 ^ 0x8d;
            }
            iVar2 = iVar2 + -1;
        } while (iVar2 != 0);

        return (byte) (uVar1 & 0xFF);
    }

}
