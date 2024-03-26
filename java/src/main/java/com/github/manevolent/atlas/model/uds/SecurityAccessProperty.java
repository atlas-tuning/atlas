package com.github.manevolent.atlas.model.uds;

import com.github.manevolent.atlas.Frame;
import com.github.manevolent.atlas.model.ProjectProperty;

import java.util.Arrays;

public class SecurityAccessProperty extends ProjectProperty {
    private int level;
    private byte[] key;

    public SecurityAccessProperty() {

    }

    public SecurityAccessProperty(int level, byte[] key) {
        this.level = level;
        this.key = key;
    }

    public int getLevel() {
        return level;
    }

    public byte[] getKey() {
        return key;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setKey(byte[] key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return "Level " + getLevel() + ": " + Frame.toHexString(key);
    }

    @Override
    public SecurityAccessProperty clone() {
        return new SecurityAccessProperty(level, Arrays.copyOf(key, key.length));
    }
}
