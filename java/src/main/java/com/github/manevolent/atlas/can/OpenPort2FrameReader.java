package com.github.manevolent.atlas.can;

import com.github.manevolent.atlas.BitReader;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

// Much appreciation for https://github.com/brandonros/rust-tactrix-openport/blob/master/src/lib.rs
public class OpenPort2FrameReader implements CanFrameReader, AutoCloseable {
    /**
     * "ar5" in ASCII
     */
    private static final byte[] HEADER = new byte[] {
            0x61,
            0x72,
            0x35
    };

    private final InputStream inputStream;

    public OpenPort2FrameReader(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public void close() throws Exception {
        inputStream.close();
    }

    @Override
    public CanFrame read() throws IOException {
        // Expect header
        byte[] tactrixHeader = new byte[HEADER.length];
        try {
            int read = inputStream.read(tactrixHeader);
            if (read < 0) {
                return null;
            } else if (read != tactrixHeader.length) {
                throw new IllegalArgumentException("Unexpected header length: " + read + " != " + tactrixHeader.length);
            }
        } catch (EOFException eof) {
            return null;
        }

        if (!Arrays.equals(tactrixHeader, HEADER)) {
            throw new IllegalArgumentException("Unexpected header: " + Arrays.toString(tactrixHeader));
        }

        int size = inputStream.read();

        byte[] header = new byte[5];
        inputStream.read(header);

        int arbitrationId = (int) new BitReader(inputStream.readNBytes(4)).read(32);

        byte[] body = new byte[size - header.length - 4];
        inputStream.read(body);

        return new OpenPort2Frame(header, arbitrationId, body);
    }

    private static class OpenPort2Frame implements CanFrame {
        private final int arbitrationId;
        private final byte[] header, body;

        private OpenPort2Frame(byte[] header, int arbitrationId, byte[] body) {
            this.header = header;
            this.arbitrationId = arbitrationId;
            this.body = body;
        }

        @Override
        public int getArbitrationId() {
            return arbitrationId;
        }

        @Override
        public byte[] getData() {
            return body;
        }
    }
}
