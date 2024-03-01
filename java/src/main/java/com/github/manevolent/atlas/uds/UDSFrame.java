package com.github.manevolent.atlas.uds;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.Frame;

import java.io.IOException;

public class UDSFrame implements Frame {
    private final Frame parent;

    private byte serviceId;
    private UDSFrameType type;
    private UDSBody body;

    private byte[] remaining;

    public UDSFrame(Frame parent) {
        this.parent = parent;
    }

    public byte getServiceId() {
        return serviceId;
    }

    public UDSBody getBody() {
        return body;
    }

    public void read() throws IOException {
        this.read(parent.bitReader());
    }

    public void read(BitReader reader) throws IOException {
        this.serviceId = reader.readByte();

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

    public UDSFrameType getType() {
        return type;
    }

    @Override
    public String toString() {
        if (type == null) {
            return String.format("Unknown 0x%02X", serviceId) + " " + parent.toString();
        }

        if (this.body == null) {
            return String.format("0x%02X", serviceId) + " "
                    + type.name() + " " + parent.toString();
        } else {
            String fullyReadWarning = remaining != null ? " remaining=" + Frame.toHexString(remaining) : "";
            return String.format("0x%02X", serviceId) + " " + body.getClass().getSimpleName()
                    + " " + body.toString() + fullyReadWarning;
        }
    }
}
