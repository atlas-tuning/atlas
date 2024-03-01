package com.github.manevolent.atlas.uds;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.BitWriter;
import com.github.manevolent.atlas.Frame;

import java.io.IOException;

public class UDSFrame implements Frame {
    private final UDSProtocol protocol;
    private UDSBody body;
    private byte[] remaining;

    public UDSFrame(UDSProtocol protocol) {
        this.protocol = protocol;
    }

    public UDSFrame(UDSProtocol protocol, UDSBody body) {
        this.protocol = protocol;
        this.body = body;
    }

    public UDSProtocol getProtocol() {
        return protocol;
    }

    public UDSBody getBody() {
        return body;
    }

    public void setBody(UDSBody body) {
        this.body = body;
    }

    public int getServiceId() {
        UDSBody body = getBody();
        if (body == null) {
            throw new NullPointerException("body");
        }

        return protocol.getSid(body.getClass());
    }

    public void write(BitWriter writer) throws IOException {
        int sid = getServiceId();
        writer.write(sid);
        getBody().write(writer);
    }

    public void read(BitReader reader) throws IOException {
        byte serviceId = reader.readByte();
        Class<? extends UDSBody> clazz = protocol.getClassBySid(serviceId);
        UDSBody body;
        try {
            body = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        try {
            body.read(reader);
        } catch (IOException ex) {
            String frameString;
            try {
                frameString = toString();
            } catch (Exception ex2) {
                ex.addSuppressed(ex2);
                frameString = "(error)";
            }

            throw new IOException("Problem reading frame " + frameString, ex);
        } catch (UnsupportedOperationException ex) {
            String frameString;
            try {
                frameString = toString();
            } catch (Exception ex2) {
                ex.addSuppressed(ex2);
                frameString = "(error)";
            }

            throw new IOException("TODO Implement " + body.getClass().getName()
                    + ": frame " + frameString, ex);
        }

        setBody(body);

        if (reader.remaining() > 0) {
            remaining = reader.readRemaining();
        }
    }

    @Override
    public byte[] getData() {
        if (body == null) {
            return null;
        }

        return body.getData();
    }

    @Override
    public String toString() {
        int sid = getServiceId();
        String fullyReadWarning = remaining != null ? " remaining=" + Frame.toHexString(remaining) : "";
        return String.format("0x%02X", sid) + " " + body.getClass().getSimpleName()
                + " " + body.toString() + fullyReadWarning;
    }
}
