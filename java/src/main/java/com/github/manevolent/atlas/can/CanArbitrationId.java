package com.github.manevolent.atlas.can;

import com.github.manevolent.atlas.Address;

public class CanArbitrationId implements Address {
    private final int arbitrationId;

    public CanArbitrationId(int arbitrationId) {
        this.arbitrationId = arbitrationId;
    }

    public int getArbitrationId() {
        return arbitrationId;
    }

    @Override
    public int hashCode() {
        return arbitrationId;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof CanArbitrationId && ((CanArbitrationId) obj).arbitrationId == this.arbitrationId);
    }

    @Override
    public String toString() {
        return Integer.toHexString(arbitrationId).toUpperCase();
    }
}
