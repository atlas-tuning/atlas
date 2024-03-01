package com.github.manevolent.atlas;

import java.io.IOException;

public class BasicFrame implements Frame {
    private byte[] data;

    public BasicFrame(byte[] data) {
        this.data = data;
    }

    public BasicFrame() {

    }

    @Override
    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public static BasicFrame from(Frame writable) throws IOException {
        return new BasicFrame(writable.write());
    }

}
