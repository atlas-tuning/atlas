package com.github.manevolent.atlas.j2534.tactrix;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.Frame;
import com.github.manevolent.atlas.can.CANFrame;
import com.github.manevolent.atlas.can.CANFrameReader;
import com.github.manevolent.atlas.j2534.J2534Error;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

// Much appreciation for https://github.com/brandonros/rust-tactrix-openport/blob/master/src/lib.rs
public class OpenPort2CANFrameReader implements CANFrameReader, AutoCloseable {
    /**
     * "ar5" in ASCII
     */
    private static final byte[] READ_DATA_HEADER = new byte[] {
            0x61,
            0x72,
            0x35
    };

    /**
     * "are" in ASCII
     */
    private static final byte[] READ_DATA_HEADER_ERROR = new byte[] {
            0x61,
            0x72,
            0x65
    };

    private static final byte[] OK_HEADER = new byte[] {
            0x61,
            0x72,
            0x6F
    };
    private final InputStream inputStream;

    public OpenPort2CANFrameReader(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public void close() throws Exception {
        inputStream.close();
    }

    @Override
    public CANFrame read() throws IOException {
        // Expect header
        byte[] tactrixHeader = new byte[READ_DATA_HEADER.length];
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

        if (Arrays.equals(tactrixHeader, READ_DATA_HEADER)) {
            int size = inputStream.read();

            byte[] frame = inputStream.readNBytes(size);
            BitReader frameReader = new BitReader(frame);

            byte[] header = new byte[5];
            frameReader.read(header);
            int arbitrationId = frameReader.readInt();
            byte[] body = frameReader.readRemaining();
            return new OpenPort2Frame(header, arbitrationId, body);
        } else if (Arrays.equals(tactrixHeader, OK_HEADER)) {
            while ((char) inputStream.read() != '\n') ;
            return null;
        } else if (Arrays.equals(tactrixHeader, READ_DATA_HEADER_ERROR)) {
            StringBuilder sb = new StringBuilder();
            while (true) {
                char c = (char) inputStream.read();
                if (c == ' ') continue;
                if (c == '\r') continue;
                if (c == '\n') break;

                sb.append(c);
            }
            int code = Integer.parseInt(sb.toString());
            J2534Error error = Arrays.stream(J2534Error.values()).filter(err -> err.getCode() == code)
                    .findFirst().orElse(null);
            throw new IOException(code + "/" + error);
        } else {
            throw new IllegalArgumentException("Unexpected header: " + Frame.toHexString(tactrixHeader));
        }
    }

    private static class OpenPort2Frame extends CANFrame {
        private final byte[] header;

        private OpenPort2Frame(byte[] header, int arbitrationId, byte[] body) {
            super(arbitrationId, body);
            this.header = header;
        }

        public byte[] getHeader() {
            return header;
        }
    }
}
