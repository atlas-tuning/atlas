package com.github.manevolent.atlas.ssm4;

import java.nio.ByteBuffer;

public class UnknownSeedKey {

    public static int shift_1(int param_1)
    {
        long uint_param_1 = (param_1 & 0xFFFFFFFFL);
        int sVar1;
        int iVar2;
        int a = (short) (uint_param_1 * 0xaa);
        int b = (short) ((uint_param_1 / 0xb2) * 0x90d);
        sVar1 = (short) ((a + b) & 0xFFFF);
        iVar2 = (int)sVar1;
        boolean term_a = iVar2 + 1 < 0;
        boolean term_b = -1 < (byte)(sVar1 >> 0xf) && iVar2 + 1 < 0;
        boolean term_c = iVar2 == -1;
        if (term_a != term_b || term_c) {
            iVar2 = (int)((sVar1 + 0x7673) & 0xFFFF);
        }
        return iVar2;
    }

    public static int shift_2(int param_1)
    {
        long uint_param_1 = (param_1 & 0xFFFFFFFFL);
        int sVar1;
        int iVar2;
        int a = (short)uint_param_1 * 0xab;
        int b = (short)(uint_param_1 / 0xb1) * 0xa03;
        sVar1 = (short) ((a + b) & 0xFFFF);
        iVar2 = (int)sVar1;
        boolean term_a = iVar2 + 1 < 0;
        boolean term_b = -1 < (byte)(sVar1 >> 0xf) && iVar2 + 1 < 0;
        boolean term_c = iVar2 == -1;
        if (term_a != term_b || term_c) {
            iVar2 = (int)((sVar1 + 0x763d) & 0xFFFF);
        }
        return iVar2;
    }

    public static int scramble_function(byte[] data, int rounds)
    {
        int uVar1;
        int uVar2;
        byte cVar3;
        int uVar4;
        int uVar5;

        uVar4 = 0xffff;
        for (uVar1 = 0; (uVar1 & 0xffff) != (rounds & 0xffff); uVar1 = uVar1 + 1) {
            uVar2 = data[uVar1 & 0xffff];
            for (cVar3 = '\0'; cVar3 != '\b'; cVar3 = (byte) (cVar3 + 1)) {
                uVar5 = uVar4 ^ uVar2;
                uVar4 = (uVar4 & 0xfffe) >> 1;
                if ((uVar5 & 1) != 0) {
                    uVar4 = uVar4 ^ 0xffff8408;
                }
                uVar2 = (uVar2 & 0xfe) >> 1;
            }
        }

        return uVar4 & 0xffff;
    }

    public static byte narrow(int value) {
        return (byte) (value & 0xFF);
    }

    public static byte[] intToByte(int data) {
        return ByteBuffer.allocate(4).putInt(data).array();
    }

    public static int generate_key(int param_1, int ram_data) {
        int iVar1;
        int uVar2;
        int uVar3;
        byte[] buffer = new byte[4];

        uVar3 = 4;
        buffer = intToByte(ram_data >> 5 & 0xffff | param_1 << 0x10);
        uVar2 = ram_data;
        iVar1 = scramble_function(buffer, 4);
        buffer = intToByte((uVar2 & 0x1fffe0) << 0xb | param_1);
        uVar2 = scramble_function(buffer, uVar3);
        return uVar2 | iVar1 << 0x10;
    }

    public static int unknownSeedKey(int param_1, int data, short param_3) {
        short uVar1;
        short uVar2;
        int iVar3;
        int uVar4;
        int iVar5;
        int uVar6;
        
        byte[] buffer = new byte[6];

        uVar2 = (short) ((param_1 >> 8) & 0xFFFF);
        uVar1 = (short) (shift_1(data >> 0x10) & 0xFFFF);
        buffer[0] = narrow(uVar1 >> 8);
        iVar5 = (int)(short)data;
        buffer[1] = narrow(uVar1);
        uVar1 = (short) (shift_2(iVar5) & 0xFFFF);
        buffer[2] = narrow(uVar1 >> 8);
        buffer[3] = narrow(uVar1);
        buffer[4] = narrow((param_3 >> 8));
        uVar6 = 6;
        buffer[5] = narrow(param_3);
        
        iVar3 = scramble_function(buffer, 6);
        buffer[0] =  narrow(uVar2 >> 8);
        buffer[1] =  narrow(uVar2);
        uVar2 = (short) (shift_1(iVar5) & 0xFFFF);
        buffer[2] =  narrow(uVar2 >> 8);
        buffer[3] =  narrow(uVar2);
        uVar2 = (short) (shift_2(param_3) & 0xFFFF);
        buffer[4] =  narrow(uVar2 >> 8);
        buffer[5] =  narrow(uVar2);
        uVar4 = scramble_function(buffer, uVar6);
        
        return uVar4 | iVar3 << 0x10;
    }

}
