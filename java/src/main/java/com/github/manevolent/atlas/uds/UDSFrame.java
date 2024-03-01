package com.github.manevolent.atlas.uds;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.BitWriter;
import com.github.manevolent.atlas.Frame;

import java.io.IOException;

public class UDSFrame implements Frame {
    private UDSBody body;
    private byte[] remaining;

    public UDSFrame() {
    }

    public UDSFrame(UDSBody body) {
        this.body = body;
    }

    public UDSBody getBody() {
        return body;
    }

    public void setBody(UDSBody body) {
        this.body = body;
    }

    public void write(BitWriter writer) throws IOException {
        writer.write(getBody().getServiceId());
        getBody().write(writer);
    }

    public void read(BitReader reader) throws IOException {
        byte serviceId = reader.readByte();
        UDSFrameType type;
        try {
            type = UDSFrameType.resolveType(serviceId);
        } catch (UnsupportedOperationException ex) {
            type = null;
        }

        UDSBody body;
        if (type != null) {
            Class<? extends UDSBody> clazz = type.resolveBodyClass(serviceId);

            try {
                body = clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            try {
                body.read(reader);
            } catch (IOException ex) {
                throw new IOException("Problem reading frame " + this.toString(), ex);
            } catch (UnsupportedOperationException ex) {
                throw new IOException("TODO Implement " + body.getClass().getName()
                        + ": frame " + this.toString(), ex);
            }

            if (reader.remaining() > 0) {
                remaining = reader.readRemaining();
            }
        } else {
            body = null;
        }

        this.body = body;
    }

    @Override
    public byte[] getData() {
        return body.getData();
    }

    @Override
    public String toString() {
        String fullyReadWarning = remaining != null ? " remaining=" + Frame.toHexString(remaining) : "";
        return String.format("0x%02X", body.getServiceId()) + " " + body.getClass().getSimpleName()
                + " " + body.toString() + fullyReadWarning;
    }
}
