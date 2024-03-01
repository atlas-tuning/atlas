package com.github.manevolent.atlas.ssm4;

import java.util.function.BiConsumer;

/**
 * This is no different than any other typical AES implementation.
 * This is not specific to SSM4/etc, but was extracted from it first to ensure it's AES.
 */
public class AES {

    /**
     * This is literally just the sbox lookup table from Rinjdael
     * But, it WAS in the disassembly verbatim
     * see: https://medium.com/codex/aes-how-the-most-advanced-encryption-actually-works-b6341c44edb9
     */
    private static final byte[] lookupTable_1 = {
            (byte)0x63,(byte)0x7c,(byte)0x77,(byte)0x7b,(byte)0xf2,(byte)0x6b,(byte)0x6f,(byte)0xc5,(byte)0x30,
            (byte)0x01,(byte)0x67,(byte)0x2b,(byte)0xfe,(byte)0xd7,(byte)0xab,(byte)0x76,(byte)0xca,(byte)0x82,
            (byte)0xc9,(byte)0x7d,(byte)0xfa,(byte)0x59,(byte)0x47,(byte)0xf0,(byte)0xad,(byte)0xd4,(byte)0xa2,
            (byte)0xaf,(byte)0x9c,(byte)0xa4,(byte)0x72,(byte)0xc0,(byte)0xb7,(byte)0xfd,(byte)0x93,(byte)0x26,
            (byte)0x36,(byte)0x3f,(byte)0xf7,(byte)0xcc,(byte)0x34,(byte)0xa5,(byte)0xe5,(byte)0xf1,(byte)0x71,
            (byte)0xd8,(byte)0x31,(byte)0x15,(byte)0x04,(byte)0xc7,(byte)0x23,(byte)0xc3,(byte)0x18,(byte)0x96,
            (byte)0x05,(byte)0x9a,(byte)0x07,(byte)0x12,(byte)0x80,(byte)0xe2,(byte)0xeb,(byte)0x27,(byte)0xb2,
            (byte)0x75,(byte)0x09,(byte)0x83,(byte)0x2c,(byte)0x1a,(byte)0x1b,(byte)0x6e,(byte)0x5a,(byte)0xa0,
            (byte)0x52,(byte)0x3b,(byte)0xd6,(byte)0xb3,(byte)0x29,(byte)0xe3,(byte)0x2f,(byte)0x84,(byte)0x53,
            (byte)0xd1,(byte)0x00,(byte)0xed,(byte)0x20,(byte)0xfc,(byte)0xb1,(byte)0x5b,(byte)0x6a,(byte)0xcb,
            (byte)0xbe,(byte)0x39,(byte)0x4a,(byte)0x4c,(byte)0x58,(byte)0xcf,(byte)0xd0,(byte)0xef,(byte)0xaa,
            (byte)0xfb,(byte)0x43,(byte)0x4d,(byte)0x33,(byte)0x85,(byte)0x45,(byte)0xf9,(byte)0x02,(byte)0x7f,
            (byte)0x50,(byte)0x3c,(byte)0x9f,(byte)0xa8,(byte)0x51,(byte)0xa3,(byte)0x40,(byte)0x8f,(byte)0x92,
            (byte)0x9d,(byte)0x38,(byte)0xf5,(byte)0xbc,(byte)0xb6,(byte)0xda,(byte)0x21,(byte)0x10,(byte)0xff,
            (byte)0xf3,(byte)0xd2,(byte)0xcd,(byte)0x0c,(byte)0x13,(byte)0xec,(byte)0x5f,(byte)0x97,(byte)0x44,
            (byte)0x17,(byte)0xc4,(byte)0xa7,(byte)0x7e,(byte)0x3d,(byte)0x64,(byte)0x5d,(byte)0x19,(byte)0x73,
            (byte)0x60,(byte)0x81,(byte)0x4f,(byte)0xdc,(byte)0x22,(byte)0x2a,(byte)0x90,(byte)0x88,(byte)0x46,
            (byte)0xee,(byte)0xb8,(byte)0x14,(byte)0xde,(byte)0x5e,(byte)0x0b,(byte)0xdb,(byte)0xe0,(byte)0x32,
            (byte)0x3a,(byte)0x0a,(byte)0x49,(byte)0x06,(byte)0x24,(byte)0x5c,(byte)0xc2,(byte)0xd3,(byte)0xac,
            (byte)0x62,(byte)0x91,(byte)0x95,(byte)0xe4,(byte)0x79,(byte)0xe7,(byte)0xc8,(byte)0x37,(byte)0x6d,
            (byte)0x8d,(byte)0xd5,(byte)0x4e,(byte)0xa9,(byte)0x6c,(byte)0x56,(byte)0xf4,(byte)0xea,(byte)0x65,
            (byte)0x7a,(byte)0xae,(byte)0x08,(byte)0xba,(byte)0x78,(byte)0x25,(byte)0x2e,(byte)0x1c,(byte)0xa6,
            (byte)0xb4,(byte)0xc6,(byte)0xe8,(byte)0xdd,(byte)0x74,(byte)0x1f,(byte)0x4b,(byte)0xbd,(byte)0x8b,
            (byte)0x8a,(byte)0x70,(byte)0x3e,(byte)0xb5,(byte)0x66,(byte)0x48,(byte)0x03,(byte)0xf6,(byte)0x0e,
            (byte)0x61,(byte)0x35,(byte)0x57,(byte)0xb9,(byte)0x86,(byte)0xc1,(byte)0x1d,(byte)0x9e,(byte)0xe1,
            (byte)0xf8,(byte)0x98,(byte)0x11,(byte)0x69,(byte)0xd9,(byte)0x8e,(byte)0x94,(byte)0x9b,(byte)0x1e,
            (byte)0x87,(byte)0xe9,(byte)0xce,(byte)0x55,(byte)0x28,(byte)0xdf,(byte)0x8c,(byte)0xa1,(byte)0x89,
            (byte)0x0d,(byte)0xbf,(byte)0xe6,(byte)0x42,(byte)0x68,(byte)0x41,(byte)0x99,(byte)0x2d,(byte)0x0f,
            (byte)0xb0,(byte)0x54,(byte)0xbb,(byte)0x16
    };

    // Formerly data at offs 1004f140
    private static final byte[] lookupTable_2 = {
            (byte)0xc6,(byte)0xf8,(byte)0xee,(byte)0xf6,(byte)0xff,(byte)0xd6,(byte)0xde,(byte)0x91,(byte)0x60,
            (byte)0x02,(byte)0xce,(byte)0x56,(byte)0xe7,(byte)0xb5,(byte)0x4d,(byte)0xec,(byte)0x8f,(byte)0x1f,
            (byte)0x89,(byte)0xfa,(byte)0xef,(byte)0xb2,(byte)0x8e,(byte)0xfb,(byte)0x41,(byte)0xb3,(byte)0x5f,
            (byte)0x45,(byte)0x23,(byte)0x53,(byte)0xe4,(byte)0x9b,(byte)0x75,(byte)0xe1,(byte)0x3d,(byte)0x4c,
            (byte)0x6c,(byte)0x7e,(byte)0xf5,(byte)0x83,(byte)0x68,(byte)0x51,(byte)0xd1,(byte)0xf9,(byte)0xe2,
            (byte)0xab,(byte)0x62,(byte)0x2a,(byte)0x08,(byte)0x95,(byte)0x46,(byte)0x9d,(byte)0x30,(byte)0x37,
            (byte)0x0a,(byte)0x2f,(byte)0x0e,(byte)0x24,(byte)0x1b,(byte)0xdf,(byte)0xcd,(byte)0x4e,(byte)0x7f,
            (byte)0xea,(byte)0x12,(byte)0x1d,(byte)0x58,(byte)0x34,(byte)0x36,(byte)0xdc,(byte)0xb4,(byte)0x5b,
            (byte)0xa4,(byte)0x76,(byte)0xb7,(byte)0x7d,(byte)0x52,(byte)0xdd,(byte)0x5e,(byte)0x13,(byte)0xa6,
            (byte)0xb9,(byte)0x00,(byte)0xc1,(byte)0x40,(byte)0xe3,(byte)0x79,(byte)0xb6,(byte)0xd4,(byte)0x8d,
            (byte)0x67,(byte)0x72,(byte)0x94,(byte)0x98,(byte)0xb0,(byte)0x85,(byte)0xbb,(byte)0xc5,(byte)0x4f,
            (byte)0xed,(byte)0x86,(byte)0x9a,(byte)0x66,(byte)0x11,(byte)0x8a,(byte)0xe9,(byte)0x04,(byte)0xfe,
            (byte)0xa0,(byte)0x78,(byte)0x25,(byte)0x4b,(byte)0xa2,(byte)0x5d,(byte)0x80,(byte)0x05,(byte)0x3f,
            (byte)0x21,(byte)0x70,(byte)0xf1,(byte)0x63,(byte)0x77,(byte)0xaf,(byte)0x42,(byte)0x20,(byte)0xe5,
            (byte)0xfd,(byte)0xbf,(byte)0x81,(byte)0x18,(byte)0x26,(byte)0xc3,(byte)0xbe,(byte)0x35,(byte)0x88,
            (byte)0x2e,(byte)0x93,(byte)0x55,(byte)0xfc,(byte)0x7a,(byte)0xc8,(byte)0xba,(byte)0x32,(byte)0xe6,
            (byte)0xc0,(byte)0x19,(byte)0x9e,(byte)0xa3,(byte)0x44,(byte)0x54,(byte)0x3b,(byte)0x0b,(byte)0x8c,
            (byte)0xc7,(byte)0x6b,(byte)0x28,(byte)0xa7,(byte)0xbc,(byte)0x16,(byte)0xad,(byte)0xdb,(byte)0x64,
            (byte)0x74,(byte)0x14,(byte)0x92,(byte)0x0c,(byte)0x48,(byte)0xb8,(byte)0x9f,(byte)0xbd,(byte)0x43,
            (byte)0xc4,(byte)0x39,(byte)0x31,(byte)0xd3,(byte)0xf2,(byte)0xd5,(byte)0x8b,(byte)0x6e,(byte)0xda,
            (byte)0x01,(byte)0xb1,(byte)0x9c,(byte)0x49,(byte)0xd8,(byte)0xac,(byte)0xf3,(byte)0xcf,(byte)0xca,
            (byte)0xf4,(byte)0x47,(byte)0x10,(byte)0x6f,(byte)0xf0,(byte)0x4a,(byte)0x5c,(byte)0x38,(byte)0x57,
            (byte)0x73,(byte)0x97,(byte)0xcb,(byte)0xa1,(byte)0xe8,(byte)0x3e,(byte)0x96,(byte)0x61,(byte)0x0d,
            (byte)0x0f,(byte)0xe0,(byte)0x7c,(byte)0x71,(byte)0xcc,(byte)0x90,(byte)0x06,(byte)0xf7,(byte)0x1c,
            (byte)0xc2,(byte)0x6a,(byte)0xae,(byte)0x69,(byte)0x17,(byte)0x99,(byte)0x3a,(byte)0x27,(byte)0xd9,
            (byte)0xeb,(byte)0x2b,(byte)0x22,(byte)0xd2,(byte)0xa9,(byte)0x07,(byte)0x33,(byte)0x2d,(byte)0x3c,
            (byte)0x15,(byte)0xc9,(byte)0x87,(byte)0xaa,(byte)0x50,(byte)0xa5,(byte)0x03,(byte)0x59,(byte)0x09,
            (byte)0x1a,(byte)0x65,(byte)0xd7,(byte)0x84,(byte)0xd0,(byte)0x82,(byte)0x29,(byte)0x5a,(byte)0x1e,
            (byte)0x7b,(byte)0xa8,(byte)0x6d,(byte)0x2c
    };

    // Formerly data at offs 1004f240
    private static final byte[] lookupTable_3 = {
            (byte)0xa5,(byte)0x84,(byte)0x99,(byte)0x8d,(byte)0x0d,(byte)0xbd,(byte)0xb1,(byte)0x54,(byte)0x50,
            (byte)0x03,(byte)0xa9,(byte)0x7d,(byte)0x19,(byte)0x62,(byte)0xe6,(byte)0x9a,(byte)0x45,(byte)0x9d,
            (byte)0x40,(byte)0x87,(byte)0x15,(byte)0xeb,(byte)0xc9,(byte)0x0b,(byte)0xec,(byte)0x67,(byte)0xfd,
            (byte)0xea,(byte)0xbf,(byte)0xf7,(byte)0x96,(byte)0x5b,(byte)0xc2,(byte)0x1c,(byte)0xae,(byte)0x6a,
            (byte)0x5a,(byte)0x41,(byte)0x02,(byte)0x4f,(byte)0x5c,(byte)0xf4,(byte)0x34,(byte)0x08,(byte)0x93,
            (byte)0x73,(byte)0x53,(byte)0x3f,(byte)0x0c,(byte)0x52,(byte)0x65,(byte)0x5e,(byte)0x28,(byte)0xa1,
            (byte)0x0f,(byte)0xb5,(byte)0x09,(byte)0x36,(byte)0x9b,(byte)0x3d,(byte)0x26,(byte)0x69,(byte)0xcd,
            (byte)0x9f,(byte)0x1b,(byte)0x9e,(byte)0x74,(byte)0x2e,(byte)0x2d,(byte)0xb2,(byte)0xee,(byte)0xfb,
            (byte)0xf6,(byte)0x4d,(byte)0x61,(byte)0xce,(byte)0x7b,(byte)0x3e,(byte)0x71,(byte)0x97,(byte)0xf5,
            (byte)0x68,(byte)0x00,(byte)0x2c,(byte)0x60,(byte)0x1f,(byte)0xc8,(byte)0xed,(byte)0xbe,(byte)0x46,
            (byte)0xd9,(byte)0x4b,(byte)0xde,(byte)0xd4,(byte)0xe8,(byte)0x4a,(byte)0x6b,(byte)0x2a,(byte)0xe5,
            (byte)0x16,(byte)0xc5,(byte)0xd7,(byte)0x55,(byte)0x94,(byte)0xcf,(byte)0x10,(byte)0x06,(byte)0x81,
            (byte)0xf0,(byte)0x44,(byte)0xba,(byte)0xe3,(byte)0xf3,(byte)0xfe,(byte)0xc0,(byte)0x8a,(byte)0xad,
            (byte)0xbc,(byte)0x48,(byte)0x04,(byte)0xdf,(byte)0xc1,(byte)0x75,(byte)0x63,(byte)0x30,(byte)0x1a,
            (byte)0x0e,(byte)0x6d,(byte)0x4c,(byte)0x14,(byte)0x35,(byte)0x2f,(byte)0xe1,(byte)0xa2,(byte)0xcc,
            (byte)0x39,(byte)0x57,(byte)0xf2,(byte)0x82,(byte)0x47,(byte)0xac,(byte)0xe7,(byte)0x2b,(byte)0x95,
            (byte)0xa0,(byte)0x98,(byte)0xd1,(byte)0x7f,(byte)0x66,(byte)0x7e,(byte)0xab,(byte)0x83,(byte)0xca,
            (byte)0x29,(byte)0xd3,(byte)0x3c,(byte)0x79,(byte)0xe2,(byte)0x1d,(byte)0x76,(byte)0x3b,(byte)0x56,
            (byte)0x4e,(byte)0x1e,(byte)0xdb,(byte)0x0a,(byte)0x6c,(byte)0xe4,(byte)0x5d,(byte)0x6e,(byte)0xef,
            (byte)0xa6,(byte)0xa8,(byte)0xa4,(byte)0x37,(byte)0x8b,(byte)0x32,(byte)0x43,(byte)0x59,(byte)0xb7,
            (byte)0x8c,(byte)0x64,(byte)0xd2,(byte)0xe0,(byte)0xb4,(byte)0xfa,(byte)0x07,(byte)0x25,(byte)0xaf,
            (byte)0x8e,(byte)0xe9,(byte)0x18,(byte)0xd5,(byte)0x88,(byte)0x6f,(byte)0x72,(byte)0x24,(byte)0xf1,
            (byte)0xc7,(byte)0x51,(byte)0x23,(byte)0x7c,(byte)0x9c,(byte)0x21,(byte)0xdd,(byte)0xdc,(byte)0x86,
            (byte)0x85,(byte)0x90,(byte)0x42,(byte)0xc4,(byte)0xaa,(byte)0xd8,(byte)0x05,(byte)0x01,(byte)0x12,
            (byte)0xa3,(byte)0x5f,(byte)0xf9,(byte)0xd0,(byte)0x91,(byte)0x58,(byte)0x27,(byte)0xb9,(byte)0x38,
            (byte)0x13,(byte)0xb3,(byte)0x33,(byte)0xbb,(byte)0x70,(byte)0x89,(byte)0xa7,(byte)0xb6,(byte)0x22,
            (byte)0x92,(byte)0x20,(byte)0x49,(byte)0xff,(byte)0x78,(byte)0x7a,(byte)0x8f,(byte)0xf8,(byte)0x80,
            (byte)0x17,(byte)0xda,(byte)0x31,(byte)0xc6,(byte)0xb8,(byte)0xc3,(byte)0xb0,(byte)0x77,(byte)0x11,
            (byte)0xcb,(byte)0xfc,(byte)0xd6,(byte)0x3a,
    };


    public static int CONCAT31(int left, byte right) {
        return left << 8 | right;
    }

    public static byte[] answer(byte[] key, byte[] challenge) {
        byte[] edi = new byte[0xFF]; // zeros
        keyExpansion(key, 0x10, edi, null);
        byte[] answer = new byte[0x10];
        aes(challenge, edi, answer);
        return answer;
    }

    public static int keyExpansion(byte[] param_1, int flag, byte[] EDI,
                                   BiConsumer<Integer, Byte> debugCallback) {
        BytePointer unaff_EDI = new BytePointer(EDI, null);
        int uVar3;
        int iVar1;
        int uVar2;
        BytePointer pbVar4 = new BytePointer(EDI, debugCallback);
        int iVar5;
        int bVar6;
        int bVar7;
        int unaff_EBX = 0;
        int uVar8;
        int _Size;
        int local_10;
        int local_9;
        int local_8;
        int local_7;
        int local_6;
        int local_5;

        uVar8 = (int)((int)unaff_EBX >> 8);
        switch(flag) {
            case 0x10:
            case 0x80:
                uVar2 = CONCAT31(uVar8, (byte) 0x10);
                flag = 0x10;
                break;
            default:
                unaff_EDI.at(0xf0, (byte) 0);
                return 0xff;
            case 0x18:
            case 0xc0:
                flag = 0x18;
                uVar2 = CONCAT31(uVar8, (byte) flag);
                break;
            case 0x20:
                uVar2 = CONCAT31(uVar8, (byte) flag);
                flag = 0x20;
        }
        _Size = uVar2 & 0xff;
        System.arraycopy(param_1, 0, unaff_EDI.backing, unaff_EDI.offs, _Size); //memcpy(unaff_EDI,param_1,_Size);
        uVar3 = (int)(uVar2 + 0x1c >> 8);
        bVar7 = ((uVar2 + 0x1c) * 0x04);
        unaff_EDI.at(0xf0, ((bVar7 >> 4) - 1));
        local_8 = 1;
        if ((byte)uVar2 < bVar7) {
            local_10 = (int)(int)(((int)((bVar7 - (int)uVar2) - 1) >> 2) + 1);
            local_9 = 0;
            pbVar4.ptr(((_Size - 3) + (int)unaff_EDI.ptr()));
            do {
                local_5 = pbVar4.at(0) & 0xFF;
                local_6 = pbVar4.at(1) & 0xFF;
                local_7 = pbVar4.at(2) & 0xFF;
                iVar1 = (int)(pbVar4.ptr() + (3 - (int)unaff_EDI.ptr())) / (int)_Size;
                iVar5 = (int)(pbVar4.ptr() + (3 - (int)unaff_EDI.ptr())) % (int)_Size;
                bVar7 = pbVar4.at(-1) & 0xFF;
                if (iVar5 == 0) {
                    uVar2 = (int)local_5 & 0xFF;
                    local_5 = lookupTable_1[local_6] & 0xFF;
                    local_6 = lookupTable_1[local_7] & 0xFF;
                    bVar6 = (lookupTable_1[uVar2] ^ local_8) & 0xFF;
                    local_7 = lookupTable_1[bVar7] & 0xFF;

                    byte al = (byte)local_8;
                    al >>= 0x7;
                    byte dl = 0x1B;
                    al = (byte) (al * -dl);
                    dl = (byte)local_8;
                    dl += dl;
                    al = (byte)(al ^ dl);

                    local_8 = al;
                    iVar1 = 0;
                    bVar7 = bVar6;
                }
                else if ((0x18 < flag) && (iVar5 == 0x10)) {
                    bVar7 = lookupTable_1[bVar7] & 0xFF;
                    local_5 = lookupTable_1[local_5] & 0xFF;
                    local_6 = lookupTable_1[local_6] & 0xFF;
                    local_7 = lookupTable_1[local_7] & 0xFF;
                    iVar1 = 0;
                }
                uVar3 = (int)((int)iVar1 >> 8);
                uVar2 = (int)local_9;
                pbVar4.at(3, (unaff_EDI.at(uVar2) ^ (byte)bVar7));
                local_9 = (local_9 + 4);
                pbVar4.at(4, (unaff_EDI.at(uVar2 + 1) ^ local_5));
                pbVar4.at(5, (unaff_EDI.at(uVar2 + 2) ^ local_6));
                local_10 = local_10 - 1;
                pbVar4.at(6, (unaff_EDI.at(uVar2 + 3) ^ local_7));
                pbVar4.addPtr(4);
            } while (local_10 != 0);
        }
        return (int)uVar3 << 8;
    }

    /**
     * Pretty sure this function takes in_EAX and unaff_ESI and produces a result
     * that is intended to be used by other functions that share the ESI register
     * @param in_EAX EAX register
     * @param unaff_ESI ESI register
     * @return
     */
    public static void lookupMath(byte[] in_EAX, byte[] unaff_ESI) {
        int bVar1;
        int bVar2;
        int bVar3;
        int bVar4;
        int bVar5;
        int bVar6;
        int bVar7;
        int bVar8;
        int bVar9;
        int bVar10;

        bVar4 = in_EAX[10] & 0xFF;
        bVar5 = lookupTable_1[bVar4] & 0xFF;
        bVar6 = lookupTable_2[in_EAX[0] & 0xFF] & 0xFF;
        bVar1 = lookupTable_3[in_EAX[5] & 0xFF] & 0xFF;
        bVar7 = lookupTable_1[in_EAX[0] & 0xFF] & 0xFF;
        bVar8 = lookupTable_2[bVar4] & 0xFF;
        bVar9 = in_EAX[0xf] & 0xFF;
        bVar2 = lookupTable_3[bVar9] & 0xFF;
        bVar3 = lookupTable_1[bVar9] & 0xFF;
        unaff_ESI[1] = (byte) (lookupTable_2[in_EAX[5] & 0xFF] ^ lookupTable_3[bVar4] ^ bVar7 ^ lookupTable_1[bVar9]);
        bVar4 = lookupTable_1[in_EAX[5] & 0xFF] & 0xFF;
        unaff_ESI[0] = (byte) (bVar6 ^ bVar1 ^ bVar5 ^ bVar3);
        bVar3 = lookupTable_2[in_EAX[4] & 0xFF] & 0xFF;
        unaff_ESI[2] = (byte) (bVar8 ^ bVar2 ^ bVar4 ^ bVar7);
        bVar6 = in_EAX[0xe] & 0xFF;
        bVar7 = lookupTable_1[bVar6] & 0xFF;
        bVar1 = lookupTable_1[in_EAX[3] & 0xFF] & 0xFF;
        unaff_ESI[3] = (byte) (lookupTable_3[in_EAX[0] & 0xFF] ^ lookupTable_2[bVar9] ^ bVar4 ^ bVar5);
        bVar4 = in_EAX[9] & 0xFF;
        bVar2 = lookupTable_1[in_EAX[4] & 0xFF] & 0xFF;
        unaff_ESI[4] = (byte) (bVar3 ^ lookupTable_3[bVar4] ^ bVar1 ^ bVar7);
        bVar3 = lookupTable_1[bVar4] & 0xFF;
        bVar5 = in_EAX[3] & 0xFF;
        unaff_ESI[5] = (byte) (lookupTable_2[bVar4] ^ lookupTable_3[bVar6] ^ bVar2 ^ bVar1);
        bVar4 = in_EAX[4] & 0xFF;
        unaff_ESI[6] = (byte) (lookupTable_3[bVar5] ^ lookupTable_2[bVar6] ^ bVar3 ^ bVar2);
        bVar2 = lookupTable_2[in_EAX[8] & 0xFF] & 0xFF;
        bVar6 = in_EAX[0xd] & 0xFF;
        bVar1 = lookupTable_3[bVar6] & 0xFF;
        bVar8 = in_EAX[7] & 0xFF;
        unaff_ESI[7] = (byte) (lookupTable_3[bVar4] ^ lookupTable_2[bVar5] ^ bVar3 ^ bVar7);
        bVar3 = lookupTable_1[in_EAX[2] & 0xFF] & 0xFF;
        unaff_ESI[8] = (byte) (bVar2 ^ bVar1 ^ bVar3 ^ lookupTable_1[bVar8]);
        bVar4 = lookupTable_1[in_EAX[8] & 0xFF] & 0xFF;
        unaff_ESI[9] = (byte) (lookupTable_3[in_EAX[2] & 0xFF] ^ lookupTable_2[bVar6] ^ bVar4 ^ lookupTable_1[bVar8]);
        bVar1 = lookupTable_1[bVar6] & 0xFF;
        bVar5 = lookupTable_3[in_EAX[8] & 0xFF] & 0xFF;
        bVar2 = lookupTable_2[bVar8] & 0xFF;
        bVar7 = in_EAX[0xc] & 0xFF;
        bVar9 = in_EAX[0xb] & 0xFF;
        unaff_ESI[10] = (byte) (lookupTable_2[in_EAX[2] & 0xFF] ^ lookupTable_3[bVar8] ^ bVar1 ^ bVar4);
        unaff_ESI[0xb] = (byte) (bVar5 ^ bVar2 ^ bVar1 ^ bVar3);
        bVar3 = in_EAX[6] & 0xFF;
        bVar5 = in_EAX[1] & 0xFF;
        bVar1 = lookupTable_1[bVar3] & 0xFF;
        bVar8 = lookupTable_2[bVar5] & 0xFF;
        bVar2 = lookupTable_3[bVar3] & 0xFF;
        bVar10 = lookupTable_2[bVar3] & 0xFF;
        bVar3 = lookupTable_3[bVar9] & 0xFF;
        bVar4 = lookupTable_1[bVar7] & 0xFF;
        unaff_ESI[0xc] = (byte) (lookupTable_3[bVar5] ^ lookupTable_2[bVar7] ^ bVar1 ^ lookupTable_1[bVar9]);
        bVar5 = lookupTable_1[bVar5] & 0xFF;
        bVar6 = lookupTable_1[bVar7] & 0xFF;
        unaff_ESI[0xd] = (byte) (bVar8 ^ bVar2 ^ bVar4 ^ lookupTable_1[bVar9]);
        unaff_ESI[0xe] = (byte) (bVar10 ^ bVar3 ^ bVar5 ^ bVar6);
        unaff_ESI[0xf] = (byte) (lookupTable_3[bVar7] ^ lookupTable_2[bVar9] ^ bVar5 ^ bVar1);

        // 0x4fcc (unaff_ESI) memory at first return of this method:
        // 63 63 63 63 63 63 63 63 63 63 63 63 63 63 63 63 00
    }

    /**
     * Shifts bytes around
     * @param in_EAX
     */
    public static void shift(byte[] in_EAX) {
        int bVar1;
        int bVar2;

        in_EAX[0] = lookupTable_1[in_EAX[0] & 0xFF];
        in_EAX[4] = lookupTable_1[in_EAX[4] & 0xFF];
        in_EAX[8] = lookupTable_1[in_EAX[8] & 0xFF];
        bVar1 = in_EAX[1] & 0xFF;
        in_EAX[0xc] = lookupTable_1[in_EAX[0xc] & 0xFF];
        in_EAX[1] = lookupTable_1[in_EAX[5] & 0xFF];
        in_EAX[5] = lookupTable_1[in_EAX[9] & 0xFF];
        in_EAX[9] = lookupTable_1[in_EAX[0xd] & 0xFF];
        bVar2 = in_EAX[2] & 0xFF;
        in_EAX[0xd] = lookupTable_1[bVar1];
        in_EAX[2] = lookupTable_1[in_EAX[10] & 0xFF];
        bVar1 = in_EAX[6] & 0xFF;
        in_EAX[10] = lookupTable_1[bVar2];
        in_EAX[6] = lookupTable_1[in_EAX[0xe] & 0xFF];
        bVar2 = in_EAX[0xf] & 0xFF;
        in_EAX[0xe] = lookupTable_1[bVar1];
        in_EAX[0xf] = lookupTable_1[in_EAX[0xb] & 0xFF];
        in_EAX[0xb] = lookupTable_1[in_EAX[7] & 0xFF];
        in_EAX[7] = lookupTable_1[in_EAX[3] & 0xFF];
        in_EAX[3] = lookupTable_1[bVar2];
    }

    public static int aes(byte[] param_1, byte[] unaff_EDI, byte[] result) {

        int i;

        byte[] mem_04fcc = new byte[16]; // aka local_10 or something?
        byte[] mem_04fdc = new byte[16]; // aka local_14?

        byte[] mem_5188 = new byte[16];
        System.arraycopy(unaff_EDI, 160, mem_5188, 0, 16);

        if (unaff_EDI[0xf0] != '\0') {
            // EBX = 0x00
            // ESI aka local_14 becomes param_1 here = 4fdc -- is 1b af ff ff 10 00 00 00 00 00 00 00 00 00 00 a0 00 00 00 00 3b 7e 00 10 f8 51 00 00 ... in memory at this tme
            // EDI/EAX = 50e8 -- this is the start of the expected byte array outside aes caller

            xor(param_1, unaff_EDI, mem_04fdc);
            i = 1;
            if (1 < unaff_EDI[0xf0]) {
                // mem_50f8 is the root
                // we add 0x10 for each pass
                int offs = 0x10;
                byte[] section = new byte[0x10];
                do {
                    System.arraycopy(unaff_EDI, offs, section, 0, 16);

                    // EAX = 0x4fdc (all zeros first pass)
                    // ESI = 0x4fcc (00 00 00 00 10 00 00 00 00 00 00 00 00 00 00 00 00) Data doesnt matter we only write to it
                    lookupMath(mem_04fdc, mem_04fcc);
                    // VERIFIED first pass, 4fcc (output) is right (all 0x63/99)

                    // EAX = 0x5108 (9b 98 98 c9 f9 fb fb aa 9b 98 98 c9 f9 fb fb aa 90) ??????
                    // ESI = 0x4fdc (00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00)
                    // param_1 = 0x4fcc (5d 7c 7c 42 5d 7c 7c 42 5d 7c 7c 42 5d 7c 7c 42 01)
                    // Hold on, right before call EAX is 0x50f8? (LATEST)
                    xor(mem_04fcc, section, mem_04fdc);
                    // After first call,
                    // ESI = 0x4fdc (01 00 00 00 01 00 00 00 01 00 00 00 01 00 00 00 00)

                    offs += 0x10;
                    i = i + 1;
                } while (i < unaff_EDI[0xf0]); // TODO: Similar problem: *(byte *)(unaff_EDI + 0xf0)
            }

            // EAX = 0x4fdc (7f fe 0e 95 51 a5 66 35 0e 34 7c 47 29 29 ec cb 00)
            shift(mem_04fdc);
            // After first call,
            // EAX = 0x4fdc (d2 06 10 1f d1 18 ce 2a ab a5 ab 96 a5 bb 33 a0 00)

            // ESI = 0x51f8 (all zeros first pass)
            // EAX = 0x5188 (b4 ef 5b cb 3e 92 e2 11 23 e9 51 cf 6f 8f 18 8e 00) I think this is static!
            // param1 = 0x4fdc (no change)
            xor(mem_04fdc, mem_5188, result);
            // After first call,
            // ESI = 0x51f8 (66 e9 4b d4 ef 8a 2c 3b 88 4c fa 59 ca 34 2b 2e 00)

            //return uVar1 & 0xffffff00;
            // uVar1 = 0x5188 (keep in mind I guess we drop the last bit?)
            // Yeah, we return 0x5100 in a couple registers
            // We end up using 0x51fc later on to copy 16 bytes from a crypto result.  Keep in mind.
            // ^ May be actual result

            // After debugging it's literally 51e6: 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
            // or 51f8: 66 e9 4b d4 ef 8a 2c 3b 88 4c fa 59 ca 34 2b 2e (likely)
            return 0;
        }

        return -1;
    }

    /**
     * returns ESI register
     * @param param_1 1st and only parameter in C
     * @param in_EAX EAX register
     * @return what is normally ESI register in C
     */
    public static void xor(byte[] param_1, byte[] in_EAX, byte[] unaff_ESI) {
        // unaff_ESI is 0x4fdc
        // in_EAX is 0x50e8 which is the output of the key expansion
        // param_1 is 0x520a which is completely all zeros at first pass
        unaff_ESI[0] = (byte) (param_1[0] ^ in_EAX[0]);
        unaff_ESI[1] = (byte) (param_1[1] ^ in_EAX[1]);
        unaff_ESI[2] = (byte) (param_1[2] ^ in_EAX[2]);
        unaff_ESI[3] = (byte) (param_1[3] ^ in_EAX[3]);
        unaff_ESI[4] = (byte) (param_1[4] ^ in_EAX[4]);
        unaff_ESI[5] = (byte) (param_1[5] ^ in_EAX[5]);
        unaff_ESI[6] = (byte) (param_1[6] ^ in_EAX[6]);
        unaff_ESI[7] = (byte) (param_1[7] ^ in_EAX[7]);
        unaff_ESI[8] = (byte) (param_1[8] ^ in_EAX[8]);
        unaff_ESI[9] = (byte) (param_1[9] ^ in_EAX[9]);
        unaff_ESI[10] = (byte) (param_1[10] ^ in_EAX[10]);
        unaff_ESI[0xb] = (byte) (param_1[0xb] ^ in_EAX[0xb]);
        unaff_ESI[0xc] = (byte) (param_1[0xc] ^ in_EAX[0xc]);
        unaff_ESI[0xd] = (byte) (param_1[0xd] ^ in_EAX[0xd]);
        unaff_ESI[0xe] = (byte) (param_1[0xe] ^ in_EAX[0xe]);
        unaff_ESI[0xf] = (byte) (param_1[0xf] ^ in_EAX[0xf]);
        // After the first pass of this operation, 0x4fdc is all zeros:
        // 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
    }

    private static byte[] ver2Sec05 = new byte[] {
            (byte)0x9A,(byte)0x71,(byte)0x1B,(byte)0x32,(byte)0xAC,(byte)0xC0,(byte)0xFF,(byte)0x40,(byte)0x89,(byte)0xA7,(byte)0x25,(byte)0x45,(byte)0x41,(byte)0x64,(byte)0x70,(byte)0xC6
    };
}
