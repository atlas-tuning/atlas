package com.github.manevolent.atlas.ssm4;

import com.github.manevolent.atlas.BitReader;

import java.io.IOException;
import java.util.function.BiConsumer;

public class BytePointer {
    public byte[] backing;
    public int offs;
    private BiConsumer<Integer, Byte> debugCallback;

    public BytePointer(byte[] backing, BiConsumer<Integer, Byte> debugCallback) {
        this.backing = backing;
        this.offs = 0;
        this.debugCallback = debugCallback;
    }

    public BytePointer(byte[] backing) {
        this.backing = backing;
        this.offs = 0;
        this.debugCallback = (offs, data) -> {};
    }

    int addPtr(int num) {
        return ptr(ptr() + num);
    }

    int ptr() {
        return offs;
    }

    int ptr(int offs) {
        return this.offs = offs;
    }

    byte at(int offs) {
        return backing[offs + this.offs];
    }

    byte at(int offs, int value) {
        if (debugCallback != null)
            debugCallback.accept(offs + this.offs, (byte) (value & 0xFF));

        return backing[offs + this.offs] = (byte) value;
    }

    short shortAt(int offs) {
        try {
            return new BitReader(backing, offs + this.offs).readShort();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}