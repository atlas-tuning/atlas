package com.github.manevolent.atlas.model.uds;

import com.github.manevolent.atlas.model.RomProperty;

public class SecurityAccessProperty extends RomProperty {
    private int level;
    private byte[] key;

    public int getLevel() {
        return level;
    }

    public byte[] getKey() {
        return key;
    }
}
