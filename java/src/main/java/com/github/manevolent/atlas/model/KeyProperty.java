package com.github.manevolent.atlas.model;

import java.util.Arrays;

public class KeyProperty extends ProjectProperty {
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

    @Override
    public ProjectProperty clone() {
        return new KeyProperty(Arrays.copyOf(key, key.length));
    }
}
