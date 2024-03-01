package com.github.manevolent.atlas.isotp;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.BitWriter;
import com.github.manevolent.atlas.Frame;

import java.io.IOException;

public class ISOTPSingleFrame extends ISOTPDataSubFrame implements Frame {
    private byte[] data;

    public ISOTPSingleFrame() {
    }

    @Override
    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public void read(BitReader reader) throws IOException {
        byte sz = (byte) reader.read(4);
        this.data = new byte[sz];
        reader.read(data);
    }

    @Override
    public void write(BitWriter writer) throws IOException {
        writer.writeNibble((byte) this.data.length);
        writer.write(this.data);
    }

    @Override
    public byte getCode() {
        return 0x0;
    }

    public ISOTPFrame coalesce() {
        return new ISOTPFrame(data);
    }
}
