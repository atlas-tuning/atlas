package com.github.manevolent.atlas;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class BitReader {
    private InputStream is;
    private long totalSize = -1;

    private Byte frame;
    private int pos, offs;

    public BitReader(byte[] frame) {
        this.is = new ByteArrayInputStream(frame);
        this.totalSize = frame.length;
    }

    public BitReader(InputStream is) {
        this.is = is;
    }

    public boolean readBoolean() throws IOException {
        int read = read();
        if (read == 1) {
            return true;
        } else if (read == 0) {
            return false;
        } else {
            throw new EOFException();
        }
    }

    private byte readNextFrame() throws IOException {
        int symbol = is.read();
        if (symbol < 0) throw new EOFException();
        byte frame = (byte)(symbol & 0xFF);
        this.frame = frame;
        return frame;
    }

    public int read() throws IOException {
        if (frame == null) {
            readNextFrame();
        } else if (this.pos >= 8){
            readNextFrame();
            this.offs ++;
            this.pos = 0;
        }

        int b = (frame >> (8 - this.pos - 1)) & 0x1;

        this.pos++;

        return b;
    }

    public int read(boolean[] bits) throws IOException {
        int offs = 0;
        for (int res; offs < bits.length && (res = read()) >= 0; offs++) {
            bits[offs] = res == 1;
        }
        return offs;
    }

    public long read(int nbits) throws IOException {
        if (nbits > 64 || nbits < 1) {
            throw new IllegalArgumentException(Integer.toString(nbits));
        }

        boolean[] bits = new boolean[nbits];
        int n = read(bits);
        if (n != nbits) {
            throw new EOFException();
        }

        long value = 0x00;
        for (int i = 0; i < nbits; i ++) {
            boolean bit = bits[i];
            if (bit) {
                long ovalue = 0x1;
                ovalue <<= nbits-i-1;
                value |= ovalue;
            }
        }

        return value;
    }


    public long read(int nbits, boolean swapOrder) throws IOException {
        if (nbits > 64 || nbits < 1) {
            throw new IllegalArgumentException(Integer.toString(nbits));
        }

        boolean[] bits = new boolean[nbits];
        int n = read(bits);
        if (n != nbits) {
            throw new EOFException();
        }

        long value = 0x00;
        for (int i = 0; i < nbits; i ++) {
            boolean bit = bits[i];
            if (bit) {
                long ovalue = 0x1;
                ovalue <<= nbits-i-1;
                value |= ovalue;
            }
        }

        return value;
    }

    public int read(byte[] bytes) throws IOException {
        int i = 0;
        for (; i < bytes.length; i ++) {
            bytes[i] = (byte) read(8);
        }
        return i;
    }

    public byte readByte() throws IOException {
        return (byte) read(8);
    }

    public short readShort() throws IOException {
        return (short) read(16);
    }

    public int readInt() throws IOException {
        return (int) read(32);
    }

    public long readLong() throws IOException {
        return (long) read(64);
    }


    public int remaining() {
        try {
            if (totalSize >= 0) {
                return (((int)totalSize - offs)*8) - pos;
            } else {
                return (is.available()*8) - pos;
            }
        } catch (IOException e) {
            return -1;
        }
    }

    public int remainingBytes() {
        return remaining() / 8;
    }

    public byte[] readRemaining() throws IOException {
        byte[] remaining = new byte[remainingBytes()];
        read(remaining);
        return remaining;
    }
}