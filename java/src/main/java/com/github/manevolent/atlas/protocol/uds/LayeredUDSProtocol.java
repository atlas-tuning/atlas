package com.github.manevolent.atlas.protocol.uds;

public class LayeredUDSProtocol implements UDSProtocol {
    private final UDSProtocol upper;
    private final UDSProtocol lower;

    public LayeredUDSProtocol(UDSProtocol upper, UDSProtocol lower) {
        this.upper = upper;
        this.lower = lower;
    }

    @Override
    public UDSQuery getBySid(int sid) throws IllegalArgumentException {
        try {
            return upper.getBySid(sid);
        } catch (IllegalArgumentException ex) {
            return lower.getBySid(sid);
        }
    }

    @Override
    public int getSid(Class<? extends UDSBody> clazz) {
        try {
            return upper.getSid(clazz);
        } catch (IllegalArgumentException ex) {
            return lower.getSid(clazz);
        }
    }
}
