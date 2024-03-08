package com.github.manevolent.atlas.model;

public class KeyProperty extends RomProperty {
    private byte[] key;

    public KeyProperty() {

    }

    public KeyProperty(byte[] key) {
        this.key = key;
    }

    public byte[] getKey() {
        return key;
    }

    public void setKey(byte[] key) {
        this.key = key;
    }
}
