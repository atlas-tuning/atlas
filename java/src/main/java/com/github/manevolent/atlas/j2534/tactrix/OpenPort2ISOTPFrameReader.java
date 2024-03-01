package com.github.manevolent.atlas.j2534.tactrix;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.Frame;
import com.github.manevolent.atlas.FrameReader;
import com.github.manevolent.atlas.can.CANArbitrationId;
import com.github.manevolent.atlas.isotp.ISOTPFrame;
import com.github.manevolent.atlas.j2534.J2534Error;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

// Much appreciation for https://github.com/brandonros/rust-tactrix-openport/blob/master/src/lib.rs
public class OpenPort2ISOTPFrameReader implements FrameReader<ISOTPFrame>, AutoCloseable {
    /**
     * "ar6" in ASCII
     */
    private static final byte[] READ_DATA_HEADER = new byte[] {
            0x61,
            0x72,
            0x36
    };

    /**
     * "are" in ASCII
     */
    private static final byte[] READ_DATA_HEADER_ERROR = new byte[] {
            0x61,
            0x72,
            0x65
    };

    /**
     * "aro" in ASCII
     */
    private static final byte[] OK_HEADER = new byte[] {
            0x61,
            0x72,
            0x6F
    };

    /**
     * "arf" in ASCII
     */
    private static final byte[] FORMAT_HEADER = new byte[] {
            0x61,
            0x72,
            0x66
    };

    private final InputStream inputStream;

    public OpenPort2ISOTPFrameReader(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public void close() throws Exception {
        inputStream.close();
    }

    @Override
    public ISOTPFrame read() throws IOException {
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
            if (body.length <= 0) {
                return null;
            }
            return new OpenPort2Frame(header, new CANArbitrationId(arbitrationId), body);
        } else if (Arrays.equals(tactrixHeader, OK_HEADER)) {
            while ((char) inputStream.read() != '\n') ;
            return null;
        } else if (Arrays.equals(tactrixHeader, READ_DATA_HEADER_ERROR) ||
                Arrays.equals(tactrixHeader, FORMAT_HEADER)) {
            char c;

            c = (char) inputStream.read();
            if (c == '6') {
                c = (char) inputStream.read();
            }

            if (c != ' ') {
                throw new IOException("Unexpected character: " + c);
            }

            StringBuilder sb = new StringBuilder();
            while (true) {

                c = (char) inputStream.read();

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

    private static class OpenPort2Frame extends ISOTPFrame {
        private final byte[] header;

        private OpenPort2Frame(byte[] header, CANArbitrationId arbitrationId, byte[] body) {
            super(arbitrationId, body);
            this.header = header;
        }

        public byte[] getHeader() {
            return header;
        }
    }
}
